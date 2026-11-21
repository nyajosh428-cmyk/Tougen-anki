package com.tougen.gun;

/**
 * Stats for the 20 real-world guns. Every gun uses the same universal bullet item.
 */
public enum GunSpec {
    // id, class, name, damage, delay(ticks), mag, reload(ticks), spread(deg), pellets, auto, range, recoil, soundPitch, blurb
    GLOCK_17("glock_17", "Pistol", "Glock 17", 6.0f, 4, 17, 40, 1.6f, 1, false, 64, 1.6f, 1.9f,
            "9x19mm striker-fired pistol, Austria, 1982"),
    M1911("m1911", "Pistol", "Colt M1911", 7.0f, 6, 7, 42, 1.2f, 1, false, 64, 2.0f, 1.6f,
            ".45 ACP single-action pistol, USA, 1911"),
    DESERT_EAGLE("desert_eagle", "Pistol", "IMI Desert Eagle", 11.0f, 12, 7, 50, 1.0f, 1, false, 72, 4.2f, 1.1f,
            ".50 AE gas-operated magnum pistol, Israel, 1983"),
    BERETTA_92FS("beretta_92fs", "Pistol", "Beretta 92FS", 6.0f, 4, 15, 40, 1.4f, 1, false, 64, 1.7f, 1.8f,
            "9x19mm double-action pistol, Italy, 1976"),
    COLT_PYTHON("colt_python", "Revolver", "Colt Python", 9.0f, 10, 6, 70, 0.8f, 1, false, 80, 3.5f, 1.3f,
            ".357 Magnum double-action revolver, USA, 1955"),

    MP5("mp5", "SMG", "Heckler & Koch MP5", 4.0f, 2, 30, 45, 2.0f, 1, true, 64, 1.2f, 2.0f,
            "9x19mm roller-delayed submachine gun, Germany, 1966"),
    UZI("uzi", "SMG", "IMI Uzi", 3.5f, 2, 32, 42, 2.6f, 1, true, 56, 1.4f, 2.0f,
            "9x19mm open-bolt submachine gun, Israel, 1954"),
    THOMPSON("thompson", "SMG", "Thompson M1928", 5.0f, 3, 30, 55, 2.4f, 1, true, 60, 1.6f, 1.5f,
            ".45 ACP 'Tommy Gun' submachine gun, USA, 1921"),

    AK47("ak47", "Rifle", "AK-47", 7.0f, 3, 30, 55, 2.2f, 1, true, 96, 2.4f, 1.4f,
            "7.62x39mm gas-operated assault rifle, USSR, 1949"),
    M4A1("m4a1", "Rifle", "Colt M4A1", 6.0f, 2, 30, 50, 1.6f, 1, true, 96, 1.8f, 1.6f,
            "5.56x45mm NATO carbine, USA, 1994"),
    SCAR_H("scar_h", "Rifle", "FN SCAR-H", 8.0f, 3, 20, 55, 1.5f, 1, true, 104, 2.6f, 1.3f,
            "7.62x51mm NATO battle rifle, Belgium, 2009"),
    STEYR_AUG("steyr_aug", "Rifle", "Steyr AUG", 6.0f, 2, 30, 50, 1.3f, 1, true, 100, 1.7f, 1.6f,
            "5.56x45mm bullpup assault rifle, Austria, 1977"),
    M1_GARAND("m1_garand", "Rifle", "M1 Garand", 9.0f, 8, 8, 45, 0.9f, 1, false, 112, 3.0f, 1.2f,
            ".30-06 semi-automatic battle rifle, USA, 1936"),
    M249_SAW("m249_saw", "LMG", "FN M249 SAW", 5.0f, 2, 100, 120, 3.0f, 1, true, 96, 2.0f, 1.5f,
            "5.56x45mm belt-fed squad automatic weapon, Belgium, 1976"),

    REMINGTON_870("remington_870", "Shotgun", "Remington 870", 2.5f, 20, 6, 60, 6.5f, 8, false, 32, 5.0f, 0.9f,
            "12-gauge pump-action shotgun, USA, 1950"),
    SPAS_12("spas_12", "Shotgun", "Franchi SPAS-12", 2.5f, 12, 8, 70, 6.0f, 8, false, 32, 5.0f, 0.9f,
            "12-gauge combat shotgun, Italy, 1979"),
    AA_12("aa_12", "Shotgun", "AA-12", 2.0f, 5, 20, 80, 7.0f, 8, true, 32, 3.5f, 1.0f,
            "12-gauge full-auto shotgun, USA, 1972"),

    REMINGTON_700("remington_700", "Sniper", "Remington 700", 20.0f, 30, 5, 60, 0.1f, 1, false, 160, 5.5f, 1.0f,
            ".308 Winchester bolt-action rifle, USA, 1962"),
    BARRETT_M82("barrett_m82", "Sniper", "Barrett M82", 30.0f, 25, 10, 90, 0.2f, 1, false, 192, 7.0f, 0.7f,
            ".50 BMG anti-materiel rifle, USA, 1982"),
    DRAGUNOV_SVD("dragunov_svd", "Sniper", "Dragunov SVD", 15.0f, 12, 10, 60, 0.4f, 1, false, 160, 4.0f, 1.2f,
            "7.62x54mmR designated marksman rifle, USSR, 1963");

    public final String id;
    public final String category;
    public final String displayName;
    public final float damage;
    public final int delay;
    public final int magSize;
    public final int reloadTicks;
    public final float spread;
    public final int pellets;
    public final boolean auto;
    public final int range;
    public final float recoil;
    public final float soundPitch;
    public final String blurb;

    GunSpec(String id, String category, String displayName, float damage, int delay, int magSize,
            int reloadTicks, float spread, int pellets, boolean auto, int range, float recoil,
            float soundPitch, String blurb) {
        this.id = id;
        this.category = category;
        this.displayName = displayName;
        this.damage = damage;
        this.delay = delay;
        this.magSize = magSize;
        this.reloadTicks = reloadTicks;
        this.spread = spread;
        this.pellets = pellets;
        this.auto = auto;
        this.range = range;
        this.recoil = recoil;
        this.soundPitch = soundPitch;
        this.blurb = blurb;
    }
}
