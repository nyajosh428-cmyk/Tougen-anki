package com.tougen.client;

import com.tougen.ModParticles;
import com.tougen.TougenMod;
import com.tougen.data.OniAbility;
import com.tougen.data.OniData;
import com.tougen.gun.GunItem;
import com.tougen.net.ActionPacket;
import com.tougen.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TougenMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private static void send(byte action) {
        Net.CHANNEL.sendToServer(new ActionPacket(action));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        while (ClientModEvents.CLASS_KEY.consumeClick()) {
            if (player != null && mc.screen == null) mc.setScreen(new ClassSelectScreen());
        }
        while (ClientModEvents.RELOAD_KEY.consumeClick()) {
            if (player != null && mc.screen == null && player.getMainHandItem().getItem() instanceof GunItem) send(ActionPacket.RELOAD);
        }
        while (ClientModEvents.BIG_GUN_KEY.consumeClick()) {
            if (player != null && mc.screen == null) send(ActionPacket.ABILITY);
        }
        while (ClientModEvents.CYCLE_KEY.consumeClick()) {
            if (player != null && mc.screen == null) send(ActionPacket.CYCLE);
        }
        if (player == null || mc.level == null) return;
        ClientHooks.tickShake(player);

        // aiming (hold right-click) and hold-to-fire for the rapid-fire gun
        if (ClientHooks.gunKind > 0 && mc.screen == null) {
            boolean aim = mc.options.keyUse.isDown();
            if (aim != ClientHooks.aiming) {
                ClientHooks.aiming = aim;
                send(aim ? ActionPacket.AIM_ON : ActionPacket.AIM_OFF);
            }
            if (ClientHooks.gunKind - 1 == OniAbility.RAPID_GUN.ordinal() && mc.options.keyAttack.isDown()) {
                send(ActionPacket.FIRE);
            }
        } else if (ClientHooks.aiming) {
            ClientHooks.aiming = false;
        }

        // Blood Legs: push into a wall to climb it, sneak to hold on
        if (ClientHooks.legsActive() && player.horizontalCollision && !player.isInWater()) {
            boolean pushing = mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                    || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
            Vec3 dm = player.getDeltaMovement();
            if (player.isShiftKeyDown()) {
                player.setDeltaMovement(dm.x, 0.0, dm.z);
            } else if (pushing) {
                double up = 0.22 * (1.0 + 0.12 * (ClientHooks.rank - 1));
                player.setDeltaMovement(dm.x, up, dm.z);
            }
            player.fallDistance = 0f;
        }

        if (!mc.isPaused() && ClientHooks.rainActive()) spawnBloodRain(mc, player);
    }

    /** With a blood gun formed: left-click shoots, right-click is aim; neither does vanilla things. */
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered e) {
        if (ClientHooks.gunKind <= 0) return;
        if (e.isAttack()) {
            send(ActionPacket.FIRE);
            e.setSwingHand(false);
            e.setCanceled(true);
        } else if (e.isUseItem()) {
            e.setSwingHand(false);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getLevel().isClientSide && ClientHooks.gunKind > 0) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent e) {
        if (ClientHooks.aiming && ClientHooks.gunKind > 0) {
            OniAbility[] all = OniAbility.values();
            int i = ClientHooks.gunKind - 1;
            if (i >= 0 && i < all.length) e.setNewFovModifier(e.getNewFovModifier() * all[i].zoom);
        }
    }

    /** Streaks of blood falling from the open sky around the player. */
    private static void spawnBloodRain(Minecraft mc, Player p) {
        RandomSource r = mc.level.random;
        for (int i = 0; i < 40; i++) {
            double x = p.getX() + (r.nextDouble() - 0.5) * 44.0;
            double z = p.getZ() + (r.nextDouble() - 0.5) * 44.0;
            double y = p.getY() + 12.0 + r.nextDouble() * 8.0;
            if (!mc.level.canSeeSky(BlockPos.containing(x, y, z))) continue;
            mc.level.addParticle(ModParticles.BLOOD_DROP.get(), x, y, z, 0.0, -0.5 - r.nextDouble() * 0.3, 0.0);
        }
    }

    /** Red-tinted sky/fog while it is raining blood. */
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor e) {
        if (!ClientHooks.rainActive()) return;
        float f = 0.65f;
        e.setRed(Mth.lerp(f, e.getRed(), 0.42f));
        e.setGreen(Mth.lerp(f, e.getGreen(), 0.01f));
        e.setBlue(Mth.lerp(f, e.getBlue(), 0.03f));
    }
}
