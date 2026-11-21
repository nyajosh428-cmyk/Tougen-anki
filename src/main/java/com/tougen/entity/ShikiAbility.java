package com.tougen.entity;

import com.tougen.ModEntities;
import com.tougen.ServerEvents;
import com.tougen.data.OniAbility;
import com.tougen.data.OniData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side Oni ability logic: summoning, cycling, firing, ranks. */
public class ShikiAbility {
    private static final Map<UUID, ShikiGunEntity> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> LAST_HIT = new HashMap<>();

    // ------------------------------------------------------------------ gun bookkeeping

    public static ShikiGunEntity gunOf(Player p) {
        ShikiGunEntity g = ACTIVE.get(p.getUUID());
        if (g != null && g.isRemoved()) {
            ACTIVE.remove(p.getUUID());
            return null;
        }
        return g;
    }

    public static boolean hasGun(Player p) {
        return gunOf(p) != null;
    }

    /** 0 = no gun, otherwise ordinal + 1 of the ability that formed it. */
    public static int gunKindIndex(Player p) {
        ShikiGunEntity g = gunOf(p);
        return g == null ? 0 : g.kind().ordinal() + 1;
    }

    public static void onGunRemoved(ShikiGunEntity gun) {
        UUID id = gun.getOwnerUuid();
        if (id == null) return;
        if (ACTIVE.get(id) == gun) ACTIVE.remove(id);
        if (gun.level() instanceof ServerLevel sl) {
            ServerPlayer p = sl.getServer().getPlayerList().getPlayer(id);
            if (p != null) {
                ServerEvents.sync(p);
                if (!isLegsActive(p)) applyExhaustion(p);
            }
        }
    }

    private static boolean isLegsActive(ServerPlayer p) {
        return OniData.getLegEnd(p) > p.level().getGameTime();
    }

    public static void dismissAll(Player p) {
        ShikiGunEntity g = ACTIVE.remove(p.getUUID());
        if (g != null) g.discard();
        LAST_HIT.remove(p.getUUID());
    }

    // ------------------------------------------------------------------ actions

    /** V key: summon the selected ability (or dismiss the gun that is already formed). */
    public static void use(ServerPlayer p) {
        if (OniData.getClassId(p) != OniData.ONI) {
            p.displayClientMessage(Component.literal("Only an Oni can do this. Press G to choose your path."), true);
            return;
        }
        ShikiGunEntity existing = gunOf(p);
        if (existing != null) {
            existing.dismiss();
            return;
        }

        int rank = OniData.getRank(p);
        OniAbility ab = OniAbility.values()[OniData.getSelected(p)];
        if (!ab.unlockedAt(rank)) {
            p.displayClientMessage(Component.literal(ab.title + " unlocks at " + OniAbility.rankName(ab.unlockRank)), true);
            return;
        }
        float cost = ab.cost(rank);
        float blood = OniData.getBlood(p);
        if (blood + 0.001f < cost) {
            p.displayClientMessage(Component.literal("Not enough blood: " + (int) blood + "% (need " + Math.round(cost) + "%)"), true);
            return;
        }

        // summoning is the ONLY thing that costs blood
        OniData.setBlood(p, blood - cost);
        ServerLevel level = p.serverLevel();
        float pm = OniAbility.power(rank);

        if (ab == OniAbility.LEG_BLOOD) {
            int ticks = (int) (20 * 60 * pm);
            OniData.setLegEnd(p, level.getGameTime() + ticks);
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5f, 0.8f);
            p.displayClientMessage(Component.literal("Blood Legs: walk into walls to climb (" + ticks / 20 + "s)"), true);
        } else {
            ShikiGunEntity gun = new ShikiGunEntity(ModEntities.SHIKI_GUN.get(), level);
            gun.setup(p, ab, pm);
            level.addFreshEntity(gun);
            ACTIVE.put(p.getUUID(), gun);
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 2.0f, 0.6f);
            p.displayClientMessage(Component.literal(ab.title + " forming... left-click to shoot, hold right-click to aim"), true);
        }

        checkExhaustion(p);
        ServerEvents.sync(p);
    }

    /**
     * Spending the last of your blood has a price, but it is paid AFTER use: the nausea and slowness
     * are only applied once the summoned gun is used up/dismissed (or the power ends).
     */
    private static void checkExhaustion(ServerPlayer p) {
        if (OniData.getBlood(p) > 0.01f) return;
        OniData.setBlood(p, 0f);
        OniData.setExhaustPending(p, true);
        p.displayClientMessage(Component.literal("All your blood is spent - the cost comes when you are done"), true);
    }

    /** Applies the 30 second nausea + slow motion if it is pending. */
    public static void applyExhaustion(ServerPlayer p) {
        if (!OniData.isExhaustPending(p)) return;
        OniData.setExhaustPending(p, false);
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, OniData.EXHAUST_TICKS, 0, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, OniData.EXHAUST_TICKS, 3, false, false));
        p.displayClientMessage(Component.literal("You are out of blood..."), true);
        p.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(0.6f, 0f, 0.02f), 1.5f),
                p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.6, 0.4, 0.0);
    }

    /** B key: switch to the next unlocked ability. */
    public static void cycle(ServerPlayer p) {
        if (OniData.getClassId(p) != OniData.ONI) return;
        if (hasGun(p)) {
            p.displayClientMessage(Component.literal("Dismiss your gun first (V)"), true);
            return;
        }
        OniAbility[] all = OniAbility.values();
        int rank = OniData.getRank(p);
        int sel = OniData.getSelected(p);
        for (int i = 1; i <= all.length; i++) {
            int idx = (sel + i) % all.length;
            if (all[idx].unlockedAt(rank)) {
                OniData.setSelected(p, idx);
                break;
            }
        }
        OniAbility now = all[OniData.getSelected(p)];
        p.displayClientMessage(Component.literal(now.title + " - " + Math.round(now.cost(rank)) + "% blood"), true);
        ServerEvents.sync(p);
    }

    public static void fire(ServerPlayer p) {
        ShikiGunEntity g = gunOf(p);
        if (g != null) g.fire(p);
    }

    public static void aim(ServerPlayer p, boolean on) {
        ShikiGunEntity g = gunOf(p);
        if (g != null) g.setAiming(on);
    }

    // ------------------------------------------------------------------ ranks

    public static void markHit(Player p) {
        LAST_HIT.put(p.getUUID(), p.level().getGameTime());
    }

    /** Called when a mob dies to this player; counts toward rank-ups if an Oni gun landed the hit. */
    public static void onKill(ServerPlayer p) {
        Long t = LAST_HIT.get(p.getUUID());
        if (t == null || p.level().getGameTime() - t > 2) return;
        int kills = OniData.getKills(p) + 1;
        OniData.setKills(p, kills);
        int rank = OniData.getRank(p);
        if (rank < 5 && kills >= OniAbility.KILLS_FOR_RANK[rank + 1]) {
            OniData.setRank(p, rank + 1);
            p.displayClientMessage(Component.literal("RANK UP! You are now " + OniAbility.rankName(rank + 1)
                    + " - cheaper blood cost, stronger abilities"), false);
            ServerEvents.sync(p);
        }
    }
}
