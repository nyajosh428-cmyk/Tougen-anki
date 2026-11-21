"""Builds Shiki Ichinose's house as a vanilla structure NBT + worldgen json.
Run: python3 tools/gen_structure.py

Minecraft axes: +x east, +z SOUTH. The front door / porch is on the low-z (north) side.
"""
import gzip
import json
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources", "data", "tougen")
SX, SY, SZ = 29, 18, 28
DATA_VERSION = 3465  # 1.20.1


# ------------------------------------------------------------------ block helpers
def B(name, **p):
    return ("minecraft:" + name if ":" not in name else name,
            tuple(sorted((k, str(v).lower() if isinstance(v, bool) else str(v)) for k, v in p.items())))


AIR = B("air")
W = {}
CHESTS = {}      # pos -> loot table
ENTITIES = []


def put(x, y, z, b):
    W[(x, y, z)] = b


def fill(x0, y0, z0, x1, y1, z1, b):
    for x in range(min(x0, x1), max(x0, x1) + 1):
        for y in range(min(y0, y1), max(y0, y1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                W[(x, y, z)] = b


def door(x, y, z, facing, name="dark_oak_door", hinge="left"):
    put(x, y, z, B(name, facing=facing, half="lower", hinge=hinge, open=False))
    put(x, y + 1, z, B(name, facing=facing, half="upper", hinge=hinge, open=False))


def chest(x, y, z, facing, loot="tougen:chests/shiki_house"):
    put(x, y, z, B("chest", facing=facing, type="single"))
    CHESTS[(x, y, z)] = loot


def frame(x, y, z, facing, item):
    f = {"north": 2, "south": 3, "west": 4, "east": 5}[facing]
    ENTITIES.append((x, y, z, f, item))


# materials
WALL = B("deepslate_bricks")
TRIM = B("polished_blackstone_bricks")
PILLAR = B("red_nether_bricks")
PLANKS = B("dark_oak_planks")
SLAB_DK = B("dark_oak_slab", type="bottom")
ROOFB = B("deepslate_tiles")
GLASS = B("black_stained_glass_pane")
FENCE = B("crimson_fence")
DFENCE = B("dark_oak_fence")


def stairs(mat, facing, half="bottom"):
    return B(mat + "_stairs", facing=facing, half=half)


# ------------------------------------------------------------------ geometry
BX0, BX1, BZ0, BZ1 = 2, 26, 5, 25     # body walls
XM, ZM = 14, 15                       # partition lines
FLOOR, CEIL1, CEIL2 = 1, 6, 11        # y layers: ground floor, upper floor, ceiling

# 1. clear volume (air), foundation, floors
fill(0, 1, 0, SX - 1, SY - 1, SZ - 1, AIR)
fill(BX0, 0, 1, BX1, 0, BZ1, TRIM)                               # foundation incl. porch
fill(BX0 + 1, FLOOR, BZ0 + 1, BX1 - 1, FLOOR, BZ1 - 1, PLANKS)    # ground floor
fill(11, FLOOR, 2, 17, FLOOR, BZ0, B("polished_deepslate"))       # porch floor + threshold
fill(3, FLOOR, 16, 13, FLOOR, 24, B("blackstone"))                # armory
fill(15, FLOOR, 6, 25, FLOOR, 14, B("polished_deepslate"))        # kitchen
fill(11, FLOOR, 6, 17, FLOOR, 8, B("polished_deepslate"))         # foyer

# 2. outer walls
for y in range(1, 11):
    fill(BX0, y, BZ0, BX1, y, BZ0, WALL)
    fill(BX0, y, BZ1, BX1, y, BZ1, WALL)
    fill(BX0, y, BZ0, BX0, y, BZ1, WALL)
    fill(BX1, y, BZ0, BX1, y, BZ1, WALL)
for y in (1, 6, 11):                                              # trim bands + slabs
    fill(BX0, y, BZ0, BX1, y, BZ1, TRIM)
fill(BX0 + 1, CEIL1, BZ0 + 1, BX1 - 1, CEIL1, BZ1 - 1, PLANKS)    # upper floor
fill(BX0 + 1, CEIL2, BZ0 + 1, BX1 - 1, CEIL2, BZ1 - 1, PLANKS)    # ceiling
for (px, pz) in [(2, 5), (26, 5), (2, 25), (26, 25), (12, 5), (16, 5), (12, 25), (16, 25), (2, 15), (26, 15)]:
    fill(px, 1, pz, px, 11, pz, PILLAR)
# exposed beams under the ceilings
for y in (5, 10):
    for z in range(6, 25, 3):
        fill(3, y, z, 25, y, z, B("stripped_dark_oak_log", axis="x"))

# 3. windows (y 3-4 ground, 8-9 upper), each with a sill and shutters
def window(x0, z0, x1, z1, y0):
    for y in (y0, y0 + 1):
        fill(x0, y, z0, x1, y, z1, GLASS)
    # sills
    if x0 == x1:
        fill(x0, y0 - 1, z0, x1, y0 - 1, z1, B("polished_blackstone_slab", type="top"))
    else:
        fill(x0, y0 - 1, z0, x1, y0 - 1, z1, B("polished_blackstone_slab", type="top"))

for (a, b) in [(5, 7), (21, 23)]:
    window(a, BZ0, b, BZ0, 3)
    window(a, BZ0, b, BZ0, 8)
window(BX0, 8, BX0, 10, 3)
window(BX1, 8, BX1, 10, 3)
window(BX1, 19, BX1, 21, 3)
window(23, BZ1, 24, BZ1, 3)
for (a, b) in [(5, 8), (20, 23)]:
    pass
window(BX0, 8, BX0, 12, 8)
window(BX1, 8, BX1, 12, 8)
window(BX1, 18, BX1, 22, 8)
window(5, BZ1, 9, BZ1, 8)
window(17, BZ1, 21, BZ1, 8)

# 4. front door, porch, balcony
door(XM, 2, BZ0, "north")
put(XM - 1, 3, BZ0 - 1, B("lantern", hanging=False))
for x in (10, 18):
    fill(x, 2, 2, x, 5, 2, FENCE)
    put(x, 5, 3, B("lantern", hanging=True))
fill(9, CEIL1, 1, 19, CEIL1, 4, B("dark_oak_slab", type="bottom"))   # porch roof = balcony floor
fill(9, CEIL1, 1, 19, CEIL1, 4, B("dark_oak_planks"))
for x in range(9, 20):
    put(x, 7, 1, FENCE)
for z in range(1, 5):
    put(9, 7, z, FENCE)
    put(19, 7, z, FENCE)
put(10, 5, 3, B("lantern", hanging=True))
put(18, 5, 3, B("lantern", hanging=True))
put(XM, 5, 3, B("lantern", hanging=True))
fill(XM, 5, 2, XM, 5, 2, B("dark_oak_slab", type="top"))
door(XM, 7, BZ0, "north")                                          # balcony door (upper lounge)
put(8, 7, 3, B("potted_dead_bush"))
put(20, 1, 3, B("potted_red_tulip")) if False else None
for x in (8, 20):
    put(x, 1, 1, B("polished_blackstone_slab", type="bottom"))
    put(x, 2, 1, B("potted_red_tulip"))
fill(XM - 1, FLOOR, 1, XM + 1, FLOOR, 1, B("deepslate_brick_stairs", facing="south"))  # step-down mat

# 5. partitions
for y in range(2, 6):
    for z in range(8, 25):
        if y in (2, 3, 4) and 20 <= z <= 21:
            continue
        put(XM, y, z, WALL)
    for x in range(3, 26):
        if y in (2, 3, 4) and (8 <= x <= 9 or 20 <= x <= 21):
            continue
        put(x, y, ZM, WALL)
for y in range(7, 11):
    for z in range(16, 25):
        if y in (7, 8, 9) and 20 <= z <= 21:
            continue
        put(XM, y, z, WALL)
    for x in range(3, 26):
        if y in (7, 8, 9) and (8 <= x <= 9 or 20 <= x <= 21):
            continue
        put(x, y, ZM, WALL)
for (x0, x1) in [(8, 9), (20, 21)]:
    fill(x0 - 1, 2, ZM, x0 - 1, 4, ZM, PILLAR)
    fill(x1 + 1, 2, ZM, x1 + 1, 4, ZM, PILLAR)
fill(XM, 2, 20 - 1, XM, 4, 20 - 1, PILLAR)
fill(XM, 2, 22, XM, 4, 22, PILLAR)

# 6. staircase to the upper floor (NE corner of living room), hole in the upper floor
fill(24, CEIL1, 16, 25, CEIL1, 20, AIR)
for k in range(5):
    put(24, 2 + k, 16 + k, stairs("dark_oak", "south"))
for z in range(16, 21):
    put(23, 7, z, DFENCE)
put(25, 7, 21, DFENCE)

# 7. roof: eaves ring, then stepped tiers
for z in range(BZ0 - 1, BZ1 + 2):
    put(BX0 - 1, CEIL2, z, stairs("deepslate_tile", "east"))
    put(BX1 + 1, CEIL2, z, stairs("deepslate_tile", "west"))
for x in range(BX0 - 1, BX1 + 2):
    put(x, CEIL2, BZ0 - 1, stairs("deepslate_tile", "south"))
    put(x, CEIL2, BZ1 + 1, stairs("deepslate_tile", "north"))
for (cx, cz) in [(BX0 - 1, BZ0 - 1), (BX1 + 1, BZ0 - 1), (BX0 - 1, BZ1 + 1), (BX1 + 1, BZ1 + 1)]:
    put(cx, CEIL2, cz, B("deepslate_tile_slab", type="bottom"))
for t in range(0, 5):
    x0, x1, z0, z1 = BX0 + 2 * t, BX1 - 2 * t, BZ0 + 2 * t, BZ1 - 2 * t
    y = CEIL2 + 1 + t
    fill(x0, y, z0, x1, y, z1, ROOFB)
    for x in range(x0, x1 + 1):
        put(x, y, z0, stairs("deepslate_tile", "south"))
        put(x, y, z1, stairs("deepslate_tile", "north"))
    for z in range(z0, z1 + 1):
        put(x0, y, z, stairs("deepslate_tile", "east"))
        put(x1, y, z, stairs("deepslate_tile", "west"))
    # keep ring flush: corners as full tiles
    for (cx, cz) in [(x0, z0), (x1, z0), (x0, z1), (x1, z1)]:
        put(cx, y, cz, ROOFB)
top = CEIL2 + 5
fill(10, top, 13, 18, top, 17, B("deepslate_tile_slab", type="bottom"))
put(14, top + 1, 15, B("lantern", hanging=False))
for dx in (-1, 1):
    put(14 + dx, top + 1, 15, B("red_nether_brick_wall"))
# chimney
fill(22, CEIL2 + 1, 19, 23, CEIL2 + 7, 20, B("blackstone"))
fill(22, CEIL2 + 8, 19, 23, CEIL2 + 8, 20, B("polished_blackstone_slab", type="bottom"))
put(22, CEIL2 + 7, 19, AIR)
put(22, CEIL2 + 7, 20, AIR)
put(22, CEIL2 + 7, 19, B("campfire", lit=True, facing="north"))

# ------------------------------------------------------------------ interiors
# --- foyer
for z in range(6, 15):
    put(XM, 2, z, B("red_carpet")) if (XM, 2, z) not in W or W[(XM, 2, z)] == AIR else None
fill(14, FLOOR + 1, 6, 14, FLOOR + 1, 7, B("red_carpet"))
put(12, 2, 7, B("dark_oak_slab", type="bottom"))     # shoe bench
put(12, 3, 7, B("potted_fern"))
put(16, 2, 7, B("barrel", facing="up"))
put(16, 3, 7, B("potted_cactus"))
put(14, 5, 7, B("lantern", hanging=True))
put(13, 4, 6, B("red_wall_banner", facing="south")) if False else None

# --- dining room (x3..13, z6..14)
fill(3, 1, 6, 13, 1, 14, PLANKS)
fill(6, 2, 9, 6, 2, 9, DFENCE); fill(10, 2, 9, 10, 2, 9, DFENCE)
fill(6, 2, 11, 6, 2, 11, DFENCE); fill(10, 2, 11, 10, 2, 11, DFENCE)
fill(6, 3, 9, 10, 3, 11, SLAB_DK)
for x in range(6, 11):
    put(x, 2, 8, stairs("dark_oak", "north"))
    put(x, 2, 12, stairs("dark_oak", "south"))
put(5, 2, 10, stairs("dark_oak", "west"))
put(11, 2, 10, stairs("dark_oak", "east"))
for (x, z) in [(8, 10), (7, 10), (9, 10)]:
    put(x, 4, z, B("candle", candles=3, lit=False)) if x == 8 else None
put(8, 4, 10, B("red_candle", candles=3, lit=True))
put(8, 5, 10, B("chain", axis="y"))
put(8, 4, 10, B("red_candle", candles=3, lit=True))
for z in range(7, 14):                                    # west pantry
    put(3, 2, z, B("barrel", facing="east"))
fill(3, 3, 7, 3, 3, 13, B("dark_oak_slab", type="bottom"))
for z in (7, 9, 11, 13):
    put(3, 4, z, B("flower_pot"))
put(3, 4, 8, B("lantern", hanging=False))
put(13, 2, 13, B("bookshelf"))
fill(4, 2, 14, 12, 2, 14, B("red_carpet"))
put(5, 5, 8, B("lantern", hanging=True))
put(11, 5, 12, B("lantern", hanging=True))
put(5, 5, 12, B("lantern", hanging=True))
put(11, 5, 8, B("lantern", hanging=True))

# --- kitchen (x15..25, z6..14)
put(25, 2, 7, B("furnace", facing="west", lit=False))
put(25, 2, 8, B("smoker", facing="west", lit=False))
put(25, 2, 9, B("blast_furnace", facing="west", lit=False))
put(25, 2, 10, B("crafting_table"))
put(25, 2, 11, B("cauldron"))
put(25, 2, 12, B("barrel", facing="west"))
chest(25, 2, 13, "west")
fill(25, 3, 7, 25, 3, 13, B("smooth_stone_slab", type="bottom"))
put(25, 4, 9, B("hanging_roots")) if False else None
fill(19, 2, 9, 21, 2, 10, B("polished_blackstone"))                 # island
fill(19, 3, 9, 21, 3, 10, B("smooth_stone_slab", type="bottom"))
for x in (19, 20, 21):
    put(x, 2, 12, B("spruce_trapdoor", facing="south", half="bottom", open=False)) if False else None
    put(x, 2, 11, stairs("dark_oak", "south"))
put(20, 4, 9, B("cake")) if False else None
put(20, 4, 10, B("red_candle", candles=2, lit=True))
for x in range(16, 25, 2):
    put(x, 5, 8, B("lantern", hanging=True))
fill(16, 2, 14, 24, 2, 14, B("barrel", facing="north"))
fill(16, 3, 14, 24, 3, 14, B("dark_oak_slab", type="top"))
fill(15, 2, 6, 15, 2, 6, B("composter"))
put(16, 2, 6, B("composter"))

# --- armory (x3..13, z16..24): the showcase of all 20 guns
GUN_ORDER = ["glock_17", "m1911", "desert_eagle", "beretta_92fs", "colt_python", "mp5", "uzi", "thompson",
             "ak47", "m4a1", "scar_h", "steyr_aug", "m1_garand", "m249_saw", "remington_870", "spas_12",
             "aa_12", "remington_700", "barrett_m82", "dragunov_svd"]
fill(BX0, 3, 17, BX0, 4, 23, B("black_concrete"))                  # backing panel, west wall
fill(5, 4, BZ1, 10, 4, BZ1, B("black_concrete"))                   # backing panel, south wall
fill(BX0, 2, 17, BX0, 2, 23, B("polished_blackstone_slab", type="top")) if False else None
gi = 0
for y in (3, 4):
    for z in range(17, 24):
        frame(3, y, z, "east", GUN_ORDER[gi]); gi += 1
for x in range(5, 11):
    frame(x, 4, 24, "north", GUN_ORDER[gi]); gi += 1
assert gi == 20, gi
for z in range(17, 24):                                            # display ledge under west frames
    put(3, 2, z, B("polished_blackstone_slab", type="bottom"))
for x in range(5, 11):
    put(x, 3, 24, B("polished_blackstone_slab", type="top"))
    put(x, 2, 24, B("polished_blackstone_slab", type="bottom"))
for z in (17, 19, 21, 23):
    put(4, 5, z, B("lantern", hanging=True))
for x in (5, 7, 9):
    put(x, 5, 23, B("lantern", hanging=True))
fill(7, 2, 16, 9, 2, 22, B("red_carpet"))                          # runner
chest(13, 2, 17, "west")
chest(13, 2, 23, "west")
put(13, 2, 18, B("smithing_table"))
put(13, 2, 19, B("grindstone", face="floor", facing="west"))
put(13, 2, 20, B("fletching_table"))
put(13, 2, 21, B("anvil", facing="north"))
put(13, 2, 22, B("barrel", facing="west"))
put(13, 3, 22, B("red_candle", candles=4, lit=True))
fill(11, 2, 24, 12, 2, 24, B("barrel", facing="north"))            # ammo crates
put(11, 3, 24, B("red_candle", candles=2, lit=True))
fill(9, 3, 16, 9, 3, 16, B("lantern", hanging=False)) if False else None
put(5, 2, 16, B("lectern", facing="east"))
put(5, 3, 16, B("red_candle", candles=1, lit=True))

# --- living room (x15..25, z16..24)
fill(16, 2, 17, 22, 2, 22, B("red_carpet")) if False else None
fill(17, 2, 18, 22, 2, 22, B("red_carpet"))
fill(17, 2, 18, 22, 2, 18, B("black_carpet"))
fill(17, 2, 22, 22, 2, 22, B("black_carpet"))
for z in range(17, 22):                                            # sofa along west partition
    put(15, 2, z, stairs("dark_oak", "west")) if z not in (20, 21) else None
put(15, 2, 17, stairs("dark_oak", "west"))
put(16, 2, 18, B("dark_oak_slab", type="bottom")) if False else None
fill(19, 2, 20, 20, 2, 20, B("dark_oak_slab", type="bottom"))     # coffee table
put(19, 3, 20, B("red_candle", candles=2, lit=True))
for x in (19, 20):
    put(x, 2, 22, stairs("dark_oak", "south")) if False else None
fill(19, 2, 24, 21, 4, 24, B("red_nether_bricks"))                 # fireplace
put(20, 2, 24, AIR)
put(20, 3, 24, AIR)
put(20, 1, 24, B("netherrack"))
put(20, 2, 24, B("fire", age=0, north=False, south=False, east=False, west=False, up=False))
fill(18, 2, 24, 18, 3, 24, B("deepslate_brick_stairs", facing="east")) if False else None
for y in (2, 3, 4):
    put(25, y, 22, B("bookshelf")); put(25, y, 23, B("bookshelf")); put(25, y, 24, B("bookshelf"))
fill(16, 2, 24, 17, 2, 24, B("dark_oak_slab", type="bottom"))
put(16, 3, 24, B("flower_pot"))
put(22, 2, 24, B("dark_oak_slab", type="bottom"))
put(22, 3, 24, B("potted_wither_rose"))
for (x, z) in [(17, 19), (22, 19)]:
    put(x, 5, z, B("lantern", hanging=True))
put(20, 5, 22, B("lantern", hanging=True))
put(16, 2, 21, stairs("dark_oak", "east")) if False else None

# --- upper lounge (x3..25, z6..14, y7..10)
fill(10, 7, 8, 18, 7, 12, B("red_carpet"))
fill(11, 7, 9, 17, 7, 11, B("black_carpet"))
for x in range(4, 9):
    put(x, 7, 14, stairs("dark_oak", "south"))
for x in range(20, 25):
    put(x, 7, 14, stairs("dark_oak", "south"))
for x in (12, 14, 16):
    put(x, 7, 10, B("dark_oak_slab", type="bottom"))
put(14, 8, 10, B("red_candle", candles=3, lit=True))
fill(4, 7, 6, 8, 7, 6, B("barrel", facing="south"))                # home bar
fill(4, 8, 6, 8, 8, 6, B("dark_oak_slab", type="top"))
put(6, 9, 6, B("brewing_stand"))  if False else None
put(5, 9, 6, B("potted_azalea_bush"))
put(7, 9, 6, B("red_candle", candles=2, lit=True))
for x in (5, 7):
    put(x, 7, 7, B("spruce_trapdoor", facing="south", half="bottom", open=False)) if False else None
for x in (24,):
    put(x, 7, 6, B("jukebox"))
    put(x, 8, 6, B("note_block")) if False else None
for (x, z) in [(6, 10), (22, 10), (14, 13)]:
    put(x, 10, z, B("chain", axis="y"))
    put(x, 9, z, B("lantern", hanging=True))

# --- study (x3..13, z16..24 upper)
for z in range(17, 24):
    for y in (7, 8, 9):
        put(3, y, z, B("bookshelf"))
put(6, 7, 20, B("dark_oak_slab", type="bottom")); put(7, 7, 20, B("dark_oak_slab", type="bottom"))
put(6, 8, 20, B("lectern", facing="south")) if False else None
put(8, 7, 20, stairs("dark_oak", "west"))
put(10, 7, 24, B("cartography_table"))
put(11, 7, 24, B("loom", facing="north"))
put(12, 7, 24, B("barrel", facing="up"))
chest(13, 7, 23, "west")
put(6, 8, 20, B("red_candle", candles=1, lit=True))
put(7, 8, 20, B("potted_oxeye_daisy")) if False else None
fill(5, 7, 17, 11, 7, 17, B("red_carpet"))
put(8, 10, 21, B("chain", axis="y"))
put(8, 9, 21, B("lantern", hanging=True))

# --- bedroom (x15..25, z16..24 upper)
put(20, 7, 23, B("red_bed", facing="south", part="foot", occupied=False))
put(20, 7, 24, B("red_bed", facing="south", part="head", occupied=False))
put(19, 7, 24, B("barrel", facing="up")); put(21, 7, 24, B("barrel", facing="up"))
put(19, 8, 24, B("red_candle", candles=2, lit=True)); put(21, 8, 24, B("red_candle", candles=2, lit=True))
fill(17, 7, 21, 23, 7, 22, B("black_carpet"))
fill(15, 7, 17, 15, 9, 17, B("chest", facing="east", type="single")) if False else None
chest(16, 7, 17, "east")
chest(16, 7, 18, "east")
put(15, 7, 19, B("anvil", facing="east")) if False else None
for y in (7, 8, 9):
    put(16, y, 24, B("bookshelf"))
put(25, 7, 24, B("smithing_table"))
put(18, 10, 20, B("chain", axis="y"))
put(18, 9, 20, B("lantern", hanging=True))
put(22, 10, 18, B("chain", axis="y"))
put(22, 9, 18, B("lantern", hanging=True))

# ------------------------------------------------------------------ post-process connections
SOLID_EXCLUDE = ("air", "carpet", "door", "lantern", "torch", "trapdoor", "slab", "stairs", "pressure_plate",
                 "button", "fire", "banner", "bed", "flower", "pot", "chain", "cauldron", "lectern", "candle",
                 "campfire", "brewing", "fence", "pane", "bars", "wall", "azalea", "fern", "cactus", "rose")


def connectable(b):
    if b is None:
        return False
    n = b[0].split(":")[1]
    if n.endswith("_pane") or n.endswith("fence") or n.endswith("_wall"):
        return True
    return not any(s in n for s in SOLID_EXCLUDE)


for pos, b in list(W.items()):
    n = b[0].split(":")[1]
    if n.endswith("_pane") or n.endswith("_fence") or n == "iron_bars":
        x, y, z = pos
        props = dict(b[1])
        for d, (dx, dz) in {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}.items():
            nb = W.get((x + dx, y, z + dz))
            props[d] = "true" if connectable(nb) else "false"
        W[pos] = (b[0], tuple(sorted(props.items())))
    elif n.endswith("_wall"):
        x, y, z = pos
        props = dict(b[1])
        props["up"] = "true"
        for d, (dx, dz) in {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}.items():
            nb = W.get((x + dx, y, z + dz))
            props[d] = "low" if connectable(nb) else "none"
        W[pos] = (b[0], tuple(sorted(props.items())))


# ------------------------------------------------------------------ NBT writer
def nbt_str(s):
    d = s.encode("utf-8")
    return struct.pack(">H", len(d)) + d


def payload(t, v):
    if t == 1:
        return struct.pack(">b", v)
    if t == 3:
        return struct.pack(">i", v)
    if t == 5:
        return struct.pack(">f", v)
    if t == 6:
        return struct.pack(">d", v)
    if t == 8:
        return nbt_str(v)
    if t == 9:
        et, items = v
        out = struct.pack(">bi", et, len(items))
        for it in items:
            out += payload(et, it)
        return out
    if t == 10:
        out = b""
        for k, (kt, kv) in v.items():
            out += struct.pack(">b", kt) + nbt_str(k) + payload(kt, kv)
        return out + b"\x00"
    raise ValueError(t)


def build_nbt():
    palette, index = [], {}
    blocks = []
    for pos in sorted(W):
        b = W[pos]
        if b not in index:
            index[b] = len(palette)
            palette.append(b)
        blk = {"pos": (9, (3, list(pos))), "state": (3, index[b])}
        if pos in CHESTS:
            blk["nbt"] = (10, {"id": (8, "minecraft:chest"), "LootTable": (8, CHESTS[pos])})
        blocks.append(blk)
    pal_tags = []
    for name, props in palette:
        e = {"Name": (8, name)}
        if props:
            e["Properties"] = (10, {k: (8, v) for k, v in props})
        pal_tags.append(e)
    ents = []
    for (x, y, z, f, item) in ENTITIES:
        nbt = {
            "id": (8, "minecraft:item_frame"),
            "Facing": (1, f), "ItemRotation": (1, 0), "ItemDropChance": (5, 1.0),
            "Invulnerable": (1, 0), "Fixed": (1, 0), "Invisible": (1, 0),
            "TileX": (3, x), "TileY": (3, y), "TileZ": (3, z),
            "Item": (10, {"id": (8, "tougen:" + item), "Count": (1, 1)}),
        }
        ents.append({"pos": (9, (6, [x + 0.5, y + 0.5, z + 0.5])),
                     "blockPos": (9, (3, [x, y, z])), "nbt": (10, nbt)})
    root = {
        "DataVersion": (3, DATA_VERSION),
        "size": (9, (3, [SX, SY, SZ])),
        "palette": (9, (10, pal_tags)),
        "blocks": (9, (10, blocks)),
        "entities": (9, (10, ents)),
    }
    return b"\x0a" + nbt_str("") + payload(10, root), len(blocks), len(palette), len(ents)


def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=1)


if __name__ == "__main__":
    raw, nb, npal, ne = build_nbt()
    p = os.path.join(ROOT, "structures", "shiki_house.nbt")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with gzip.open(p, "wb") as f:
        f.write(raw)
    print(f"shiki_house.nbt: {nb} blocks, {npal} palette entries, {ne} item frames, {len(CHESTS)} chests")

    wj(os.path.join(ROOT, "worldgen", "structure", "shiki_house.json"), {
        "type": "minecraft:jigsaw",
        "biomes": "#tougen:has_shiki_house",
        "step": "surface_structures",
        "spawn_overrides": {},
        "terrain_adaptation": "beard_thin",
        "start_pool": "tougen:shiki_house",
        "size": 1,
        "max_distance_from_center": 40,
        "use_expansion_hack": False,
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "start_height": {"absolute": -1},
    })
    wj(os.path.join(ROOT, "worldgen", "template_pool", "shiki_house.json"), {
        "fallback": "minecraft:empty",
        "elements": [{"weight": 1, "element": {
            "element_type": "minecraft:single_pool_element",
            "location": "tougen:shiki_house",
            "processors": "minecraft:empty",
            "projection": "rigid"}}],
    })
    wj(os.path.join(ROOT, "worldgen", "structure_set", "shiki_house.json"), {
        "structures": [{"structure": "tougen:shiki_house", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 12, "separation": 5,
                      "salt": 58213046, "frequency": 0.3},
    })
    wj(os.path.join(ROOT, "tags", "worldgen", "biome", "has_shiki_house.json"),
       {"values": ["#minecraft:is_overworld"]})
