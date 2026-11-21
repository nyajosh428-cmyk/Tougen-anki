package com.tougen.entity;

/**
 * Shared (client + server) description of how the blood gun assembles itself.
 * Parts: 0 receiver, 1 barrel, 2 muzzle, 3 stock, 4 grip, 5 magazine, 6 scope, 7 rails, 8 horns.
 * START/END are fractions of the ability's build time; POS is each part's centre in model pixels
 * (x right, y UP, z forward) so the server can aim blood tendrils at the part that is forming.
 */
public class GunParts {
    public static final int COUNT = 9;
    public static final float[] START = {0.00f, 0.20f, 0.45f, 0.30f, 0.45f, 0.55f, 0.60f, 0.65f, 0.75f};
    public static final float[] END = {0.25f, 0.45f, 0.55f, 0.50f, 0.60f, 0.70f, 0.75f, 0.80f, 0.90f};
    public static final float[][] POS = {
            {0, 0, 0}, {0, 0, 30}, {0, 0, 46}, {0, -1, -25}, {0, -14, -6},
            {0, -16, 4}, {0, 8, -2}, {7, 0, 10}, {4, 10, 10}};

    /** Ease-out-back: the part pops slightly past full size, then settles - feels like it congeals. */
    public static float stage(float t, int part) {
        float x = Math.max(0f, Math.min(1f, (t - START[part]) / (END[part] - START[part])));
        if (x <= 0f) return 0f;
        float c1 = 1.70158f, c3 = c1 + 1f;
        float u = x - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }
}
