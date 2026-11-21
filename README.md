# Tougen Guns (Forge 1.20.1, Java 17)

## Build / run
1. Install JDK 17.
2. `./gradlew runClient` (generate the wrapper first with `gradle wrapper` if you only have the files), or `gradle build` for the jar in `build/libs`.
   (ForgeGradle downloads Minecraft/Forge, so it needs internet.)

## Controls
| Key | Action |
|-----|--------|
| G | Oni / Momotaro selection screen |
| R | Reload held (real) gun |
| V | Summon the selected Oni ability (or dismiss the blood gun that is already formed) |
| B | Switch to the next unlocked Oni ability |
| Left click | Fire the blood gun (hold for the rapid-fire gun) |
| Right click (hold) | Aim: zoom + steadier shots |

## What's in it
- 20 real guns with 3D box models; one universal `Bullet` item. Guns are NOT craftable.
- Bullet recipe: 2 iron ingots with a gap between (`I _ I`), gives 8 bullets. Unlocks when you pick up iron.
- Shiki's house: 30% of spawn regions in the Overworld, 20 guns in item frames in the armory, loot chests with 10-20 bullets. Vanilla chests also get 10-20 bullets (45% chance).

## Oni
Blood is shown as a percent (HUD, bottom left) and refills from 0 to 100% in 5 minutes. Blood is spent ONLY when you summon.
If a summon uses up all your blood you get nausea + slowness for 30 seconds, but only AFTER the gun has been used up or dismissed (or Blood Legs ends).

| School (rank) | Ability | Summon cost at unlock |
|---|---|---|
| 1st | Shiki's Big Gun (beam cannon, 1 shot) | 100% (all your blood; 80/60/40/20% at ranks 2-5) |
| 2nd | Blood Pistol (12 shots) | 10% |
| 3rd | Blood Sniper (3 shots, huge range) | 30% |
| 4th | Blood Legs (walk into walls to climb) | 40% |
| 4th | Blood Rapid-Fire (60 rounds, full-auto) | 40% |
| Professional | Cannon that erases a woodland mansion (1 shot) | 50% |

The Big Gun drops 20% per rank, down to 20% at Professional. Other abilities: each rank above their unlock rank makes them 12% cheaper (min 40% of base) and every rank adds +20% power.
Rank up by getting kills with Oni guns (10 / 30 / 60 / 100 kills for ranks 2-5), or `/tougen rank <1-5>` (op).
Firing any blood gun, the Big Gun, Sniper or Cannon, at the open sky makes it rain blood for 30 seconds.
The Professional cannon removes every non-bedrock block in a ~32 block crater plus a tunnel along the beam, so use it carefully.

## Regenerating assets
`python3 tools/gen_assets.py` (needs Pillow) and `python3 tools/gen_structure.py`.
