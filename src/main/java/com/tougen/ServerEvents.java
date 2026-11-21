package com.tougen;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tougen.data.OniAbility;
import com.tougen.data.OniData;
import com.tougen.entity.ShikiAbility;
import com.tougen.net.Net;
import com.tougen.net.SyncPacket;
import com.tougen.world.BloodRain;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

public class ServerEvents {
    private static final DustParticleOptions LEG_DUST = new DustParticleOptions(new Vector3f(0.65f, 0.0f, 0.02f), 1.0f);

    public static void sync(ServerPlayer p) {
        long now = p.level().getGameTime();
        int leg = (int) Math.max(0, Math.min(Integer.MAX_VALUE, OniData.getLegEnd(p) - now));
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),
                new SyncPacket(OniData.getClassId(p), OniData.getBlood(p), OniData.getRank(p),
                        OniData.getSelected(p), ShikiAbility.gunKindIndex(p), leg));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        if (OniData.getClassId(p) != OniData.ONI) return;

        float blood = OniData.getBlood(p);
        if (blood < OniData.MAX_BLOOD) {
            OniData.setBlood(p, blood + OniData.REGEN_PER_TICK);   // 0 -> 100% in 5 minutes
            if (p.tickCount % 10 == 0) sync(p);
        }

        long legEnd = OniData.getLegEnd(p);
        long now = p.level().getGameTime();
        if (legEnd > now) {
            if (p.tickCount % 3 == 0) {
                p.serverLevel().sendParticles(LEG_DUST, p.getX(), p.getY() + 0.1, p.getZ(), 2, 0.25, 0.05, 0.25, 0.0);
            }
            p.fallDistance = 0;
        } else if (legEnd != 0) {
            OniData.setLegEnd(p, 0);
            p.displayClientMessage(Component.literal("Blood Legs faded"), true);
            if (!ShikiAbility.hasGun(p)) ShikiAbility.applyExhaustion(p);
            sync(p);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            sync(p);
            BloodRain.syncTo(p);
            if (!ShikiAbility.hasGun(p) && OniData.getLegEnd(p) <= p.level().getGameTime()) ShikiAbility.applyExhaustion(p);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        ShikiAbility.dismissAll(e.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            ShikiAbility.dismissAll(p);
            sync(p);
            BloodRain.syncTo(p);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            if (OniData.getClassId(p) == OniData.ONI) OniData.setBlood(p, OniData.MAX_BLOOD);
            OniData.setLegEnd(p, 0);
            OniData.setExhaustPending(p, false);
            sync(p);
            BloodRain.syncTo(p);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        CompoundTag old = e.getOriginal().getPersistentData();
        if (old.contains(Player.PERSISTED_NBT_TAG)) {
            e.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG,
                    old.getCompound(Player.PERSISTED_NBT_TAG).copy());
        }
    }

    /** Rank-ups: kills made with Oni guns count. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p && OniData.getClassId(p) == OniData.ONI) {
            ShikiAbility.onKill(p);
        }
    }

    /** While a blood gun is formed, left-click is for shooting, not for breaking blocks or punching. */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (!e.getLevel().isClientSide && ShikiAbility.hasGun(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent e) {
        if (!e.getEntity().level().isClientSide && ShikiAbility.hasGun(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("tougen")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("rank")
                        .then(Commands.argument("rank", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    int r = IntegerArgumentType.getInteger(ctx, "rank");
                                    OniData.setRank(p, r);
                                    OniData.setKills(p, OniAbility.KILLS_FOR_RANK[r]);
                                    sync(p);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Rank set to " + OniAbility.rankName(r)), false);
                                    return 1;
                                }))));
    }

    /** Scatter 10-20 bullets into vanilla chests (and everything else with a chest loot table). */
    @SubscribeEvent
    public static void onLootTable(LootTableLoadEvent e) {
        if (!e.getName().getNamespace().equals("minecraft") || !e.getName().getPath().startsWith("chests/")) return;
        e.getTable().addPool(LootPool.lootPool()
                .name("tougen_bullets")
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.45f))
                .add(LootItem.lootTableItem(ModItems.BULLET.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(10f, 20f))))
                .build());
    }
}
