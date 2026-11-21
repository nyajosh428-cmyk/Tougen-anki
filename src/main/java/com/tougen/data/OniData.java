package com.tougen.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Per-player class, blood percent, school rank and ability selection. Stored in the player's persistent
 * data so it survives logout and death without needing a capability.
 */
public class OniData {
    public static final int NONE = 0;
    public static final int ONI = 1;
    public static final int MOMOTARO = 2;

    public static final float MAX_BLOOD = 100f;
    /** Full refill from empty takes 5 minutes = 6000 ticks. */
    public static final float REGEN_PER_TICK = MAX_BLOOD / (5f * 60f * 20f);
    /** Nausea + slowness when the blood runs out. */
    public static final int EXHAUST_TICKS = 30 * 20;

    private static final String ROOT = "tougen_class_data";

    private static CompoundTag data(Player p) {
        CompoundTag persistent = p.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
            persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(ROOT)) {
            persisted.put(ROOT, new CompoundTag());
        }
        return persisted.getCompound(ROOT);
    }

    public static int getClassId(Player p) {
        return data(p).getInt("Class");
    }

    public static void setClassId(Player p, int id) {
        CompoundTag d = data(p);
        d.putInt("Class", id);
        if (id == ONI) d.putFloat("Blood", MAX_BLOOD);
    }

    public static float getBlood(Player p) {
        CompoundTag d = data(p);
        return d.contains("Blood") ? d.getFloat("Blood") : MAX_BLOOD;
    }

    public static void setBlood(Player p, float v) {
        data(p).putFloat("Blood", Math.max(0f, Math.min(MAX_BLOOD, v)));
    }

    public static int getRank(Player p) {
        int r = data(p).getInt("Rank");
        return r < 1 ? 1 : Math.min(r, 5);
    }

    public static void setRank(Player p, int rank) {
        data(p).putInt("Rank", Math.max(1, Math.min(5, rank)));
    }

    public static int getKills(Player p) {
        return data(p).getInt("Kills");
    }

    public static void setKills(Player p, int k) {
        data(p).putInt("Kills", k);
    }

    public static int getSelected(Player p) {
        int s = data(p).getInt("Sel");
        OniAbility[] all = OniAbility.values();
        return s < 0 || s >= all.length ? 0 : s;
    }

    public static void setSelected(Player p, int s) {
        data(p).putInt("Sel", s);
    }

    public static long getLegEnd(Player p) {
        return data(p).getLong("LegEnd");
    }

    public static void setLegEnd(Player p, long t) {
        data(p).putLong("LegEnd", t);
    }

    /** Out-of-blood side effects wait until the summoned gun / power has been used. */
    public static boolean isExhaustPending(Player p) {
        return data(p).getBoolean("ExhaustPending");
    }

    public static void setExhaustPending(Player p, boolean v) {
        data(p).putBoolean("ExhaustPending", v);
    }
}
