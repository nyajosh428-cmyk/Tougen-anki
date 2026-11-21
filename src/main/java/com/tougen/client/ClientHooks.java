package com.tougen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

/** Client-side mirrored state and packet callbacks. Only ever touched from the client. */
public class ClientHooks {
    public static int classId = 0;
    public static float blood = 100f;
    public static int rank = 1;
    public static int selected = 0;
    /** 0 = no blood gun, else ability ordinal + 1 */
    public static int gunKind = 0;
    public static boolean aiming = false;
    /** game time at which wall-walking ends (0 = inactive) */
    public static long legEnd = 0L;
    private static long rainEnd = -1L;

    public static void onSync(int c, float b, int r, int sel, int gun, int legTicks) {
        classId = c;
        blood = b;
        rank = r;
        selected = sel;
        gunKind = gun;
        if (gun == 0) aiming = false;
        Level level = Minecraft.getInstance().level;
        legEnd = (legTicks > 0 && level != null) ? level.getGameTime() + legTicks : 0L;
    }

    public static boolean legsActive() {
        Level level = Minecraft.getInstance().level;
        return level != null && legEnd > level.getGameTime();
    }

    /** camera shake after a heavy shot */
    public static float shakeAmp = 0f;
    public static int shakeTicks = 0;

    public static void onRecoil(float pitch, float yaw) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;
        p.setXRot(p.getXRot() - pitch);
        p.setYRot(p.getYRot() + yaw);
        if (pitch >= 3f) {
            shakeAmp = Math.min(2.5f, pitch * 0.2f);
            shakeTicks = 12;
        }
    }

    public static void tickShake(LocalPlayer p) {
        if (shakeTicks <= 0) return;
        float k = shakeTicks / 12f;
        p.setXRot(p.getXRot() + (p.getRandom().nextFloat() - 0.5f) * shakeAmp * k);
        p.setYRot(p.getYRot() + (p.getRandom().nextFloat() - 0.5f) * shakeAmp * k);
        shakeTicks--;
    }

    public static void onRain(int ticks) {
        Level level = Minecraft.getInstance().level;
        rainEnd = (ticks > 0 && level != null) ? level.getGameTime() + ticks : -1L;
    }

    public static boolean rainActive() {
        Level level = Minecraft.getInstance().level;
        return level != null && rainEnd >= 0 && level.getGameTime() < rainEnd;
    }
}
