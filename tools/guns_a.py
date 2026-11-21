"""Pistols, revolver, submachine guns."""
from gunlib import *

CX = 8


def pistol(g, zf=3.5, sl=8.0, top=10.4, sh=2.0, slide=D, frame=P, panel=None, bw=1.8,
           hammer=False, safety=False, rail=False, cut=False, grip_h=6.4, mag=D, comp=False):
    zb = zf + sl
    y0 = top - sh
    g.b(CX, y0, zf, bw, sh, sl, slide)                         # slide
    g.b(CX, top, zf + 0.3, bw * 0.55, 0.2, sl - 0.6, slide)    # top rib
    for i in range(5):                                         # rear serrations
        g.b(CX, y0 + 0.25, zb - 1.9 + i * 0.34, bw + 0.08, sh - 0.5, 0.13, P)
    g.b(CX, top, zf + 0.3, 0.4, 0.55, 0.4, P)                  # front sight
    g.b(CX, top + 0.4, zf + 0.3, 0.2, 0.2, 0.08, WH)
    g.b(CX - 0.6, top, zb - 1.0, 0.35, 0.5, 0.6, P)            # rear sight
    g.b(CX + 0.6, top, zb - 1.0, 0.35, 0.5, 0.6, P)
    g.b(CX + bw / 2 + 0.03, y0 + 0.45, zf + sl * 0.38, 0.1, sh - 0.9, sl * 0.32, P)   # ejection port
    g.b(CX, y0 + 0.45, zf - 0.7, 1.0, sh - 0.9, 0.8, M)        # barrel tip
    g.b(CX, y0 + 0.9, zf - 0.74, 0.5, 0.5, 0.06, P)            # bore
    fy = y0 - 1.25
    g.b(CX, fy, zf + 0.8, bw - 0.1, 1.3, sl - 1.0, frame)      # frame
    if rail:
        for i in range(3):
            g.b(CX, fy - 0.15, zf + 1.2 + i * 0.85, 1.5, 0.25, 0.4, S)
    tg = 1.5
    g.b(CX, fy - tg + 0.3, zf + sl * 0.42, bw - 0.3, tg, 0.45, frame)       # guard front
    g.b(CX, fy - tg + 0.3, zf + sl * 0.42, bw - 0.3, 0.45, sl * 0.30, frame)  # guard bottom
    g.b(CX, fy - 0.9, zf + sl * 0.62, 0.45, 1.2, 0.4, M)       # trigger
    gz = zf + sl * 0.70
    gl = zb - gz + 0.7
    n = 4
    seg = grip_h / n
    for i in range(n):
        g.b(CX, fy - seg * (i + 1) + 0.02, gz + 0.3 * i, bw + 0.1, seg, gl, frame)
        if panel is not None:
            for sx in (-1, 1):
                g.b(CX + sx * ((bw + 0.1) / 2 + 0.04), fy - seg * (i + 1) + 0.12, gz + 0.3 * i + 0.3,
                    0.1, seg - 0.24, gl - 0.6, panel)
        else:
            for sx in (-1, 1):   # stippling
                for k in range(2):
                    g.b(CX + sx * ((bw + 0.1) / 2 + 0.03), fy - seg * (i + 1) + 0.3 + k * 0.5,
                        gz + 0.3 * i + 0.8 + k * 1.2, 0.07, 0.3, 0.5, M)
    g.b(CX, fy - grip_h - 0.3, gz + 0.3 * (n - 1) + 0.05, bw + 0.25, 0.45, gl + 0.15, mag)   # base plate
    if hammer:
        g.b(CX, top - 0.7, zb - 0.1, 0.5, 1.1, 0.8, M)
        g.b(CX, top + 0.1, zb + 0.4, 0.8, 0.25, 0.5, M)
    if safety:
        g.b(CX + bw / 2 + 0.07, y0 - 0.2, zb - 2.3, 0.15, 0.5, 0.9, M)
    if cut:
        g.b(CX, top - 0.12, zf + 1.0, 0.9, 0.2, 3.2, S)
        g.b(CX, top - 0.1, zf + 1.4, 0.5, 0.15, 2.4, P)
    if comp:
        for i in range(3):
            g.b(CX, top - 0.05, zf + 0.7 + i * 0.5, 0.9, 0.15, 0.2, P)
    g.anchor = (CX, fy - 1.2, gz + 1.0)


def glock17():
    g = G()
    pistol(g, zf=3.5, sl=8.0, top=10.4, slide=D, frame=P, panel=None)
    g.b(CX, 4.0, 9.6, 0.3, 0.3, 0.3, M)          # lanyard nub
    g.b(CX + 0.95, 7.0, 6.2, 0.1, 0.45, 1.1, M)  # slide stop
    g.b(CX, 6.6, 5.6, 0.5, 0.3, 0.3, WH)         # loaded chamber marker
    return g


def m1911():
    g = G()
    pistol(g, zf=3.3, sl=7.8, top=10.3, sh=1.9, slide=BL, frame=BL, panel=W, hammer=True, safety=True,
           grip_h=6.0, bw=1.7)
    g.b(CX, 6.0, 9.8, 0.9, 1.6, 0.6, S)          # grip safety
    g.b(CX, 4.4, 10.3, 1.5, 0.35, 2.6, D)        # mainspring housing
    g.b(CX, 9.3, 3.0, 1.1, 0.8, 0.5, S)          # barrel bushing
    for i in range(3):                           # wood panel screws / checkering
        g.b(CX + 1.0, 4.0 + i * 1.3, 11.2, 0.05, 0.3, 0.3, BR)
    return g


def desert_eagle():
    g = G()
    pistol(g, zf=2.0, sl=10.6, top=11.7, sh=2.7, slide=CH, frame=D, panel=P, bw=2.3, rail=True,
           grip_h=6.8, mag=D)
    g.rail(CX, 11.7, 2.4, 12.2, w=1.3, slots=True, c=D, slot_c=P, pitch=0.8)
    g.b(CX, 8.0, 0.8, 1.4, 1.5, 1.4, D)                  # barrel wedge
    g.b(CX, 8.2, 0.3, 0.8, 0.9, 0.6, M)
    g.b(CX, 9.4, 0.4, 0.3, 0.9, 0.3, OR)                 # front sight dot
    g.b(CX + 1.2, 8.6, 6.0, 0.1, 1.2, 3.8, P)            # ejection port
    g.b(CX + 1.2, 8.8, 9.6, 0.1, 0.6, 1.2, RD)           # safety
    return g


def beretta92():
    g = G()
    pistol(g, zf=3.2, sl=8.6, top=10.5, sh=2.0, slide=D, frame=D, panel=P, hammer=True, safety=True,
           cut=True, grip_h=6.2, bw=1.9)
    g.b(CX, 3.2, 12.5, 0.4, 0.8, 0.5, S)          # lanyard ring
    g.b(CX, 6.4, 5.4, 1.4, 0.25, 3.0, D)          # open trigger guard bar
    g.b(CX + 1.0, 8.3, 5.6, 0.1, 0.7, 1.4, S)     # takedown lever
    return g


def colt_python():
    g = G()
    g.b(CX, 8.9, 0.8, 1.35, 1.4, 6.4, BL)                              # barrel
    g.b(CX, 7.7, 1.4, 1.2, 1.2, 5.2, BL)                               # underlug
    g.b(CX, 10.3, 1.0, 0.75, 0.4, 6.0, BL)                             # vent rib
    for i in range(8):
        g.b(CX, 10.45, 1.3 + i * 0.7, 0.8, 0.12, 0.26, P)
    g.b(CX, 10.3, 0.8, 0.5, 0.8, 0.6, RD)                              # red ramp sight
    g.b(CX, 8.9, 0.74, 0.6, 0.6, 0.06, P)
    g.b(CX, 7.3, 7.2, 1.7, 3.5, 3.0, BL)                               # frame
    g.cyl_z(CX, 8.9, 7.2, 3.0, 1.55, S)                                # cylinder
    for i in range(6):                                                 # flutes
        g.b(CX + 1.58, 8.3, 7.5 + i * 0.5, 0.06, 1.2, 0.2, P)
        g.b(CX - 1.58, 8.3, 7.5 + i * 0.5, 0.06, 1.2, 0.2, P)
    g.b(CX, 7.3, 10.2, 1.7, 3.5, 1.9, BL)                              # rear frame
    g.b(CX, 10.4, 10.0, 1.1, 0.5, 2.2, BL)                             # top strap
    g.b(CX, 10.9, 11.2, 1.2, 0.5, 0.6, P)                              # adjustable rear sight
    g.b(CX, 9.9, 12.0, 0.6, 1.5, 0.9, S)                               # hammer
    g.b(CX, 11.2, 12.2, 0.8, 0.2, 0.5, S)
    g.b(CX, 5.0, 8.6, 1.5, 0.4, 3.4, BL)                               # trigger guard bottom
    g.b(CX, 5.0, 8.6, 1.5, 2.4, 0.4, BL)                               # guard front
    g.b(CX, 5.8, 10.2, 0.45, 1.5, 0.4, M)                              # trigger
    for i in range(4):                                                 # walnut grip
        g.b(CX, 7.4 - 1.05 * (i + 1), 11.2 + 0.5 * i, 1.9, 1.05, 2.6, W)
    g.b(CX, 2.9, 12.4, 1.95, 0.35, 2.7, WD)
    g.b(CX, 6.6, 11.0, 0.5, 0.5, 0.5, BR)                              # grip medallion
    g.anchor = (CX, 4.5, 12.0)
    return g


def mp5():
    g = G()
    g.b(CX, 8.0, 3.5, 2.4, 2.4, 8.5, P)                                # receiver
    g.b(CX, 7.6, 0.6, 2.8, 2.8, 4.2, P)                                # handguard
    for i in range(4):
        g.b(CX, 8.0, 1.0 + i * 0.85, 2.9, 0.3, 0.4, D)
        g.b(CX, 9.2, 1.0 + i * 0.85, 2.9, 0.3, 0.4, D)
    g.b(CX, 8.9, 0.0, 0.9, 0.9, 0.7, M)                                # barrel tip
    g.b(CX, 8.8, -0.35, 1.5, 1.5, 0.5, D)                              # tri-lug hider
    g.b(CX, 10.4, 0.6, 1.5, 0.9, 1.1, P)                               # front drum sight
    g.b(CX, 11.2, 0.9, 0.3, 0.5, 0.3, D)
    g.b(CX, 10.4, 10.4, 1.3, 0.9, 1.1, P)                              # rear drum sight
    for i in range(3):                                                 # retractable wire stock
        g.b(CX - 0.7, 7.2, 12.0 + i * 1.4, 0.3, 0.35, 1.2, M)
        g.b(CX + 0.7, 7.2, 12.0 + i * 1.4, 0.3, 0.35, 1.2, M)
    g.b(CX, 6.0, 15.5, 1.7, 4.2, 0.5, P)
    g.steps(CX, 7.6, 6.6, 5, 1.5, -0.45, 1.6, 2.2, M)                  # curved 30rd mag
    for i in range(5):
        g.b(CX, 7.6 - 1.5 * (i + 1) + 0.6, 6.6 - 0.45 * i + 0.05, 1.7, 0.2, 2.1, D)
    g.steps(CX, 7.6, 10.6, 4, 1.35, 0.5, 1.7, 1.9, P)                  # pistol grip
    g.b(CX, 5.4, 8.8, 1.1, 0.3, 1.6, P)                                # trigger guard
    g.b(CX, 5.6, 9.2, 0.35, 1.2, 0.35, M)
    g.b(CX - 1.4, 9.0, 4.6, 0.5, 0.5, 1.6, M)                          # cocking tube
    g.b(CX + 1.25, 8.6, 10.0, 0.3, 0.4, 0.9, M)                        # selector
    g.b(CX + 1.22, 9.0, 6.5, 0.05, 1.0, 1.2, D)                        # ejection port
    g.anchor = (CX, 5.5, 11.2)
    return g


def uzi():
    g = G()
    g.b(CX, 7.6, 3.0, 2.6, 2.6, 8.2, D)                                # receiver
    for i in range(7):
        g.b(CX, 8.0, 3.3 + i * 1.05, 2.7, 0.18, 0.4, P)                # stamped ribs
    g.b(CX, 8.2, 0.6, 1.8, 1.8, 2.6, D)                                # shroud
    g.b(CX, 8.6, 0.0, 1.0, 1.0, 0.8, M)                                # barrel nut
    g.b(CX, 10.2, 4.0, 1.6, 0.35, 6.0, D)                              # top cover
    g.b(CX, 10.5, 5.0, 1.6, 0.5, 1.0, P)                               # cocking knob
    g.b(CX - 0.9, 10.2, 0.9, 0.3, 0.9, 0.4, D)                         # front sight guards
    g.b(CX + 0.9, 10.2, 0.9, 0.3, 0.9, 0.4, D)
    g.b(CX, 10.2, 0.9, 0.25, 0.7, 0.25, D)
    g.b(CX, 10.2, 10.0, 1.2, 0.6, 0.9, P)                              # flip sight
    g.b(CX, 1.2, 7.6, 2.2, 6.4, 3.0, P)                                # pistol grip / magwell
    g.b(CX, 0.5, 7.6, 2.3, 0.7, 3.0, D)                                # base plate
    for i in range(4):
        g.b(CX + 1.12, 2.0 + i * 1.3, 8.0, 0.06, 0.4, 2.2, M)
        g.b(CX - 1.12, 2.0 + i * 1.3, 8.0, 0.06, 0.4, 2.2, M)
    g.b(CX, 6.4, 6.2, 1.2, 0.3, 1.5, D)                                # trigger guard
    g.b(CX, 6.5, 6.8, 0.35, 1.0, 0.35, M)
    for i in range(3):                                                 # folded-out wire stock
        g.b(CX - 0.8, 7.0, 11.2 + i * 1.6, 0.3, 0.3, 1.4, M)
        g.b(CX + 0.8, 7.0, 11.2 + i * 1.6, 0.3, 0.3, 1.4, M)
    g.b(CX, 5.4, 15.6, 1.6, 3.6, 0.4, M)
    g.b(CX + 1.4, 8.2, 9.0, 0.3, 0.5, 0.8, M)                          # selector
    g.anchor = (CX, 3.0, 9.0)
    return g


def thompson():
    g = G()
    g.b(CX, 9.0, 0.3, 1.2, 1.2, 5.6, D)                                # barrel
    for i in range(9):                                                 # cooling fins
        g.b(CX, 8.75, 0.9 + i * 0.5, 1.7, 1.7, 0.22, D)
    g.b(CX, 8.9, -0.5, 1.6, 1.5, 1.0, D)                               # Cutts compensator
    for i in range(3):
        g.b(CX, 9.7, -0.4 + i * 0.3, 1.7, 0.15, 0.12, P)
    g.b(CX, 7.1, 3.0, 2.4, 1.9, 3.4, W)                                # wooden forend
    g.b(CX, 3.4, 3.6, 1.1, 3.8, 1.2, WD)                               # vertical foregrip
    g.b(CX, 3.2, 3.5, 1.3, 0.4, 1.4, WD)
    g.b(CX, 7.4, 5.9, 2.2, 3.0, 5.4, BL)                               # receiver
    for i in range(5):
        g.b(CX, 7.6, 6.2 + i * 0.9, 2.3, 0.15, 0.25, P)
    g.b(CX, 10.4, 10.7, 1.0, 0.7, 1.0, D)                              # ladder sight
    g.b(CX, 10.2, 1.0, 0.3, 0.8, 0.3, D)
    g.b(CX + 1.3, 8.6, 6.8, 0.6, 0.6, 0.6, P)                          # cocking knob
    g.cyl_x(CX, 3.6, 8.3, 1.9, 3.0, D)                                 # 50-round drum
    g.cyl_x(CX, 3.6, 8.3, 2.05, 2.0, M)
    g.cyl_x(CX, 3.6, 8.3, 2.1, 0.8, S)
    g.steps(CX, 7.4, 10.4, 4, 1.2, 0.4, 1.7, 1.8, W)                   # wood pistol grip
    g.b(CX, 5.4, 9.0, 1.2, 0.3, 1.6, D)
    g.b(CX, 6.0, 12.0, 1.8, 3.0, 3.8, W)                               # stock
    g.b(CX, 4.8, 15.0, 1.8, 4.8, 1.0, WD)
    g.b(CX, 4.6, 15.9, 1.9, 5.0, 0.3, D)
    g.anchor = (CX, 5.0, 11.0)
    return g
