"""Generates every texture / model / lang / data json for the mod. Run: python3 tools/gen_assets.py"""
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(__file__))
from PIL import Image, ImageDraw

import gunlib
import guns_a
import guns_b

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "tougen")
DATA = os.path.join(ROOT, "data", "tougen")


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=1)


GUNS = [  # (id, display name, builder) - ids must match GunSpec
    ("glock_17", "Glock 17", guns_a.glock17),
    ("m1911", "Colt M1911", guns_a.m1911),
    ("desert_eagle", "IMI Desert Eagle", guns_a.desert_eagle),
    ("beretta_92fs", "Beretta 92FS", guns_a.beretta92),
    ("colt_python", "Colt Python", guns_a.colt_python),
    ("mp5", "Heckler & Koch MP5", guns_a.mp5),
    ("uzi", "IMI Uzi", guns_a.uzi),
    ("thompson", "Thompson M1928", guns_a.thompson),
    ("ak47", "AK-47", guns_b.ak47),
    ("m4a1", "Colt M4A1", guns_b.m4a1),
    ("scar_h", "FN SCAR-H", guns_b.scar_h),
    ("steyr_aug", "Steyr AUG", guns_b.steyr_aug),
    ("m1_garand", "M1 Garand", guns_b.m1_garand),
    ("m249_saw", "FN M249 SAW", guns_b.m249),
    ("remington_870", "Remington 870", guns_b.remington_870),
    ("spas_12", "Franchi SPAS-12", guns_b.spas12),
    ("aa_12", "AA-12", guns_b.aa12),
    ("remington_700", "Remington 700", guns_b.remington_700),
    ("barrett_m82", "Barrett M82", guns_b.barrett_m82),
    ("dragunov_svd", "Dragunov SVD", guns_b.dragunov_svd),
]


def palette_texture():
    img = Image.new("RGBA", (16, 16))
    for i, c in enumerate(gunlib.PALETTE):
        sx, sy = (i % 4) * 4, (i // 4) * 4
        for x in range(4):
            for y in range(4):
                img.putpixel((sx + x, sy + y), c + (255,))
    p = os.path.join(ASSETS, "textures", "item", "gun_palette.png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def bullet_texture():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # diagonal brass casing with copper tip, drawn as stacked pixels
    for i in range(7):                       # casing
        x, y = 3 + i, 12 - i
        d.rectangle([x, y, x + 2, y + 2], fill=(201, 162, 39, 255))
        d.point((x, y), fill=(240, 205, 90, 255))
        d.point((x + 2, y + 2), fill=(140, 105, 20, 255))
    for i in range(4):                       # jacketed tip
        x, y = 10 + i, 5 - i
        d.rectangle([x, y, x + 1, y + 1], fill=(184, 98, 52, 255))
        d.point((x, y), fill=(225, 140, 90, 255))
    d.rectangle([2, 12, 4, 14], fill=(110, 80, 15, 255))   # primer rim
    d.point((3, 13), fill=(190, 190, 195, 255))
    img.save(os.path.join(ASSETS, "textures", "item", "bullet.png"))


def shiki_texture():
    random.seed(7)
    img = Image.new("RGBA", (256, 256))
    for x in range(256):
        for y in range(256):
            n = random.randint(-14, 14)
            vein = 40 if (x * 7 + y * 3) % 53 < 4 else 0
            img.putpixel((x, y), (max(0, min(255, 118 + n + vein)), 4 + abs(n) // 4, 10 + abs(n) // 3, 255))
    d = ImageDraw.Draw(img)
    for i in range(60):                       # dark clots
        x, y, r = random.randint(0, 255), random.randint(0, 255), random.randint(2, 8)
        d.ellipse([x - r, y - r, x + r, y + r], fill=(60, 0, 4, 255))
    img.save(os.path.join(ASSETS, "textures", "entity", "shiki_gun.png"))


def blood_drop_texture():
    img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 1, 4, 6], fill=(170, 6, 12, 255))
    d.rectangle([3, 1, 3, 5], fill=(225, 30, 34, 255))
    d.point((3, 7), fill=(120, 0, 6, 255))
    d.point((4, 7), fill=(120, 0, 6, 255))
    img.save(os.path.join(ASSETS, "textures", "particle", "blood_drop.png"))
    w(os.path.join(ASSETS, "particles", "blood_drop.json"), {"textures": ["tougen:blood_drop"]})


def models_and_lang():
    lang = {
        "itemGroup.tougen": "Tougen Guns",
        "item.tougen.bullet": "Bullet",
        "entity.tougen.shiki_gun": "Shiki's Big Gun",
        "key.categories.tougen": "Tougen Guns",
        "key.tougen.class_menu": "Choose Oni / Momotaro",
        "key.tougen.reload": "Reload Gun",
        "key.tougen.big_gun": "Summon Oni ability / dismiss gun",
        "key.tougen.cycle": "Switch Oni ability",
    }
    w(os.path.join(ASSETS, "models", "item", "bullet.json"),
      {"parent": "item/generated", "textures": {"layer0": "tougen:item/bullet"}})
    for gid, name, build in GUNS:
        g = build()
        model = gunlib.gunmodel(g, gid)
        w(os.path.join(ASSETS, "models", "item", gid + ".json"), model)
        lang["item.tougen." + gid] = name
        print(f"{gid:15s} {len(g.els):3d} boxes")
    w(os.path.join(ASSETS, "lang", "en_us.json"), lang)


def data_files():
    w(os.path.join(DATA, "recipes", "bullet.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "pattern": ["I I"],      # two iron ingots with a gap; shaped recipes ignore grid position
        "key": {"I": {"item": "minecraft:iron_ingot"}},
        "result": {"item": "tougen:bullet", "count": 8},
    })
    w(os.path.join(DATA, "advancements", "recipes", "bullet.json"), {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_iron": {"trigger": "minecraft:inventory_changed",
                         "conditions": {"items": [{"items": ["minecraft:iron_ingot"]}]}},
            "has_the_recipe": {"trigger": "minecraft:recipe_unlocked",
                               "conditions": {"recipe": "tougen:bullet"}},
        },
        "requirements": [["has_the_recipe", "has_iron"]],
        "rewards": {"recipes": ["tougen:bullet"]},
    })
    w(os.path.join(DATA, "loot_tables", "chests", "shiki_house.json"), {
        "type": "minecraft:chest",
        "pools": [
            {"rolls": {"min": 2, "max": 3}, "entries": [
                {"type": "minecraft:item", "name": "tougen:bullet", "weight": 12,
                 "functions": [{"function": "minecraft:set_count", "count": {"min": 10, "max": 20}}]},
                {"type": "minecraft:item", "name": "minecraft:iron_ingot", "weight": 6,
                 "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 5}}]},
                {"type": "minecraft:item", "name": "minecraft:cooked_beef", "weight": 5,
                 "functions": [{"function": "minecraft:set_count", "count": {"min": 3, "max": 8}}]},
                {"type": "minecraft:item", "name": "minecraft:golden_apple", "weight": 1},
                {"type": "minecraft:item", "name": "minecraft:emerald", "weight": 3,
                 "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 3}}]},
            ]},
            {"rolls": 1, "entries": [
                {"type": "minecraft:item", "name": "tougen:bullet", "weight": 1,
                 "functions": [{"function": "minecraft:set_count", "count": {"min": 10, "max": 20}}]},
            ]},
        ],
    })


if __name__ == "__main__":
    palette_texture()
    bullet_texture()
    shiki_texture()
    blood_drop_texture()
    models_and_lang()
    data_files()
    print("assets done")
