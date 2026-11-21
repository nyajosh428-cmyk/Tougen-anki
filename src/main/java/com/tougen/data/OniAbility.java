package com.tougen.data;

/**
 * Oni abilities. Each unlocks at a school rank (1st..5th/Professional) and costs blood ONLY to summon.
 * Cost drops and power rises with every rank above the unlock rank / above 1st school.
 */
public enum OniAbility {
    //            name,                  unlock, cost%, build, ammo, delay, dmg,  spread, range, sx,   sy,   sz,   fwd,  right, down, zoom
    BIG_GUN("Shiki's Big Gun",            1, 100f,  70,   1,   0,  30f, 0.0f,  96, 1.10f, 1.10f, 1.10f, 3.4f, 0.00f, 0.45f, 0.60f),
    SMALL_GUN("Blood Pistol",             2, 10f,  25,  12,   4,   6f, 1.2f,  64, 0.40f, 0.40f, 0.40f, 1.5f, 0.55f, 0.50f, 0.80f),
    SNIPER("Blood Sniper",                3, 30f,  45,   3,  25,  28f, 0.0f, 256, 0.50f, 0.50f, 1.00f, 2.4f, 0.50f, 0.45f, 0.20f),
    RAPID_GUN("Blood Rapid-Fire",         4, 40f,  35,  60,   2,   4f, 2.2f,  80, 0.55f, 0.55f, 0.55f, 1.9f, 0.55f, 0.50f, 0.70f),
    LEG_BLOOD("Blood Legs (wall walking)",4, 40f,   0,   0,   0,   0f, 0.0f,   0, 0f,    0f,    0f,    0f,   0f,    0f,    1.00f),
    MEGA_GUN("Professional Cannon",       5, 50f, 100,   1,   0,  80f, 0.0f, 120, 2.60f, 2.60f, 2.60f, 8.0f, 0.00f, 1.50f, 0.55f);

    public final String title;
    public final int unlockRank;
    public final float baseCost;
    public final int buildTicks;
    public final int ammo;
    public final int delay;
    public final float damage;
    public final float spread;
    public final int range;
    public final float sx, sy, sz;
    public final float fwd, right, down;
    public final float zoom;

    OniAbility(String title, int unlockRank, float baseCost, int buildTicks, int ammo, int delay, float damage,
               float spread, int range, float sx, float sy, float sz, float fwd, float right, float down, float zoom) {
        this.title = title;
        this.unlockRank = unlockRank;
        this.baseCost = baseCost;
        this.buildTicks = buildTicks;
        this.ammo = ammo;
        this.delay = delay;
        this.damage = damage;
        this.spread = spread;
        this.range = range;
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.fwd = fwd;
        this.right = right;
        this.down = down;
        this.zoom = zoom;
    }

    public boolean isGun() {
        return this != LEG_BLOOD;
    }

    public boolean unlockedAt(int rank) {
        return rank >= unlockRank;
    }

    /** Blood % to summon at this rank: listed cost at the unlock rank, 12% cheaper for each rank above it. */
    public float cost(int rank) {
        if (this == BIG_GUN) {   // all your blood at 1st school, 20% less each rank, 20% at Professional
            return Math.max(20f, 100f - 20f * (Math.max(1, rank) - 1));
        }
        float mult = Math.max(0.4f, 1f - 0.12f * Math.max(0, rank - unlockRank));
        return baseCost * mult;
    }

    /** Power multiplier: +20% per rank above 1st school. */
    public static float power(int rank) {
        return 1f + 0.2f * (Math.max(1, rank) - 1);
    }

    public static final String[] RANK_NAMES = {"", "1st School", "2nd School", "3rd School", "4th School", "Professional"};
    /** Oni-gun kills needed to reach rank 2..5 */
    public static final int[] KILLS_FOR_RANK = {0, 0, 10, 30, 60, 100};

    public static String rankName(int rank) {
        return RANK_NAMES[Math.max(1, Math.min(5, rank))];
    }
}
