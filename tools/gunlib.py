"""Tiny box-model DSL for building 3D gun item models (Minecraft JSON elements).

Coordinate convention (pixels, 16 = 1 block):
  x: left/right, gun centred on x=8     y: up         z: muzzle at small z, butt at large z
Barrel points toward -Z (north). Display transforms in gunmodel() rotate it for each view.
"""
import json
import math

# palette swatch indices (4x4 grid of 4px blocks in a 16x16 texture)
D, M, S, P, WD, W, BR, CH, RD, OD, TN, WH, GL, RB, OR, BL = range(16)
PALETTE = [
    (0x2b, 0x2d, 0x31), (0x4a, 0x4e, 0x57), (0x8a, 0x91, 0x9c), (0x16, 0x17, 0x1a),
    (0x4e, 0x2f, 0x17), (0x7b, 0x51, 0x28), (0xc9, 0xa2, 0x27), (0xc9, 0xce, 0xd6),
    (0xa0, 0x19, 0x19), (0x4b, 0x5a, 0x2e), (0xb8, 0xa2, 0x78), (0xe8, 0xe8, 0xe8),
    (0x3a, 0x6e, 0xa5), (0x0c, 0x0c, 0x0e), (0xe0, 0x7a, 0x10), (0x2d, 0x3a, 0x4d),
]


class G:
    def __init__(self):
        self.els = []
        self.anchor = (8, 5, 10)   # where the hand holds the gun

    def b(self, cx, y, z, w, h, l, c):
        """Box: centre x, bottom y, front z, width, height, length."""
        self.els.append((cx - w / 2, y, z, cx + w / 2, y + h, z + l, c))

    def box(self, x0, y0, z0, x1, y1, z1, c):
        self.els.append((x0, y0, z0, x1, y1, z1, c))

    # -------- composite helpers
    def steps(self, cx, ytop, z0, n, dy, dz, w, l, c):
        """Stack of n boxes going down, each shifted dz in z (grips, curved mags)."""
        for i in range(n):
            self.b(cx, ytop - dy * (i + 1), z0 + dz * i, w, dy, l, c)

    def cyl_z(self, cx, cy, z0, l, r, c):
        """Octagon-ish cylinder along z."""
        self.b(cx, cy - r * 0.42, z0, r * 2, r * 0.84, l, c)
        self.b(cx, cy - r, z0, r * 0.84, r * 2, l, c)
        self.b(cx, cy - r * 0.72, z0, r * 1.44, r * 1.44, l, c)

    def cyl_x(self, cx, cy, cz, w, r, c):
        """Octagon-ish disc/drum whose axis is x, centred (cx, cy, cz)."""
        self.b(cx, cy - r * 0.42, cz - r, w, r * 0.84, r * 2, c)
        self.b(cx, cy - r, cz - r * 0.42, w, r * 2, r * 0.84, c)
        self.b(cx, cy - r * 0.72, cz - r * 0.72, w, r * 1.44, r * 1.44, c)

    def rail(self, cx, y, z0, z1, w=1.2, slots=True, c=D, slot_c=P, pitch=0.7):
        self.b(cx, y, z0, w, 0.3, z1 - z0, c)
        if slots:
            z = z0 + 0.3
            while z < z1 - 0.4:
                self.b(cx, y + 0.02, z, w + 0.02, 0.3, 0.25, slot_c)
                z += pitch

    def vents(self, cx, y, z0, n, w, h, pitch, c=P, depth=0.12):
        for i in range(n):
            self.b(cx, y, z0 + i * pitch, w, h, depth, c)

    # -------- output
    def bounds(self):
        xs = [e[0] for e in self.els] + [e[3] for e in self.els]
        ys = [e[1] for e in self.els] + [e[4] for e in self.els]
        zs = [e[2] for e in self.els] + [e[5] for e in self.els]
        return (min(xs), min(ys), min(zs)), (max(xs), max(ys), max(zs))


def uv_for(c):
    sx, sy = (c % 4) * 4, (c // 4) * 4
    return [sx + 1, sy + 1, sx + 3, sy + 3]


def rot_matrix(rx, ry, rz):
    """Rx * Ry * Rz (degrees) - same order as Minecraft's display rotation."""
    ax, ay, az = (math.radians(a) for a in (rx, ry, rz))
    cx, sx = math.cos(ax), math.sin(ax)
    cy, sy = math.cos(ay), math.sin(ay)
    cz, sz = math.cos(az), math.sin(az)
    Rx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Ry = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Rz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]

    def mm(A, B):
        return [[sum(A[i][k] * B[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mm(Rx, mm(Ry, Rz))


def centred_translation(rot, scale, point):
    """Translation so that model point (in px) lands at the display origin."""
    R = rot_matrix(*rot)
    v = [(point[i] - 8) * scale for i in range(3)]
    t = [-sum(R[i][k] * v[k] for k in range(3)) for i in range(3)]
    return [round(max(-80, min(80, x)), 2) for x in t]


def gunmodel(g, name):
    """Return the model JSON dict for gun builder g."""
    lo, hi = g.bounds()
    centre = [(lo[i] + hi[i]) / 2 for i in range(3)]
    length = hi[2] - lo[2]
    height = hi[1] - lo[1]

    elements = []
    for i, (x0, y0, z0, x1, y1, z1, c) in enumerate(g.els):
        e = i * 0.0007  # later boxes win coplanar z-fights
        uv = uv_for(c)
        face = {"uv": uv, "texture": "#0"}
        elements.append({
            "from": [round(x0 - e, 4), round(y0 - e, 4), round(z0 - e, 4)],
            "to": [round(x1 + e, 4), round(y1 + e, 4), round(z1 + e, 4)],
            "faces": {d: dict(face) for d in ("north", "east", "south", "west", "up", "down")},
        })

    span = max(length, height * 1.2)
    gui_s = round(min(1.75, 15.0 / span), 3)
    gui_rot = [18, -90, 0]
    fixed_rot = [0, -90, 0]
    fixed_s = round(min(1.3, 13.0 / span), 3)
    ground_s = 0.55
    fp_s = 0.8
    tp_s = 0.62
    tp_rot = [90, 0, 180]
    anchor = g.anchor

    display = {
        "gui": {"rotation": gui_rot, "scale": [gui_s] * 3,
                "translation": centred_translation(gui_rot, gui_s, centre)},
        "ground": {"rotation": [0, 0, 0], "scale": [ground_s] * 3,
                   "translation": centred_translation([0, 0, 0], ground_s, centre)},
        "fixed": {"rotation": fixed_rot, "scale": [fixed_s] * 3,
                  "translation": centred_translation(fixed_rot, fixed_s, centre)},
        "head": {"rotation": [0, 180, 0], "scale": [0.8] * 3, "translation": [0, 8, 0]},
    }
    # first person: barrel straight ahead, grip near the hand, nudged down-right of centre
    fp_t = centred_translation([0, 0, 0], fp_s, anchor)
    fp_t = [fp_t[0], fp_t[1] - 1.0, fp_t[2] - 1.5]
    fp_l = [-fp_t[0], fp_t[1], fp_t[2]]
    display["firstperson_righthand"] = {"rotation": [0, 0, 0], "scale": [fp_s] * 3, "translation": fp_t}
    display["firstperson_lefthand"] = {"rotation": [0, 0, 0], "scale": [fp_s] * 3, "translation": fp_l}
    tp_t = centred_translation(tp_rot, tp_s, anchor)
    display["thirdperson_righthand"] = {"rotation": tp_rot, "scale": [tp_s] * 3, "translation": tp_t}
    display["thirdperson_lefthand"] = {"rotation": tp_rot, "scale": [tp_s] * 3, "translation": tp_t}

    return {
        "credit": "Generated by tools/gen_assets.py",
        "gui_light": "side",
        "textures": {"0": "tougen:item/gun_palette", "particle": "tougen:item/gun_palette"},
        "elements": elements,
        "display": display,
    }
