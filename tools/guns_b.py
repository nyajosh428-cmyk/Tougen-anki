"""Rifles, LMG, shotguns, snipers."""
from gunlib import *

CX = 8


def ak47():
    g = G()
    g.b(CX, 9.4, -0.2, 0.9, 0.9, 3.6, D)                               # barrel
    g.b(CX, 9.3, -0.6, 1.3, 1.1, 0.6, D)                               # slant muzzle brake
    g.b(CX, 10.0, 1.0, 0.5, 1.1, 0.8, D)                               # front sight post
    g.b(CX, 9.6, 3.0, 1.2, 1.4, 1.2, D)                                # gas block
    g.b(CX, 10.6, 3.5, 1.1, 0.8, 4.0, D)                               # gas tube
    g.b(CX, 10.4, 4.2, 1.8, 1.2, 3.4, W)                               # upper handguard
    g.b(CX, 7.8, 3.2, 2.0, 2.4, 4.2, W)                                # lower handguard
    for i in range(4):
        g.b(CX, 8.2 + (i % 2) * 0.8, 3.6 + i * 0.9, 2.1, 0.12, 0.3, WD)
    g.b(CX, 7.8, 7.4, 2.0, 2.8, 5.0, D)                                # receiver
    g.b(CX, 10.6, 7.6, 1.8, 0.2, 4.6, D)                               # dust cover rib
    g.b(CX + 1.15, 9.4, 8.8, 0.5, 0.5, 1.2, M)                         # charging handle
    g.b(CX, 10.8, 7.0, 0.9, 0.6, 1.8, D)                               # rear sight leaf
    g.b(CX + 1.05, 8.8, 9.4, 0.15, 0.4, 2.2, M)                        # selector lever
    g.steps(CX, 7.8, 8.6, 6, 1.2, -0.55, 1.4, 2.6, D)                  # banana magazine
    for i in range(6):
        g.b(CX, 7.8 - 1.2 * (i + 1) + 0.5, 8.6 - 0.55 * i + 0.05, 1.5, 0.15, 2.5, M)
    g.steps(CX, 7.8, 11.6, 4, 1.15, 0.4, 1.7, 1.8, WD)                 # bakelite grip
    g.b(CX, 5.8, 9.8, 1.3, 0.35, 1.8, D)
    g.b(CX, 6.0, 10.0, 0.4, 1.0, 0.35, M)
    g.b(CX, 6.6, 12.6, 1.8, 3.8, 3.3, W)                               # stock
    g.b(CX, 5.6, 15.4, 1.8, 4.8, 0.6, D)
    g.anchor = (CX, 5.5, 11.8)
    return g


def m4a1():
    g = G()
    g.b(CX, 9.2, -0.4, 0.8, 0.8, 1.2, P)
    g.b(CX, 9.1, -0.7, 1.3, 1.0, 0.8, D)                               # birdcage hider
    g.b(CX, 9.7, 2.2, 0.9, 1.6, 0.8, D)                                # front sight tower
    g.b(CX, 11.3, 2.3, 0.25, 0.6, 0.25, D)
    g.b(CX, 7.8, 0.8, 2.5, 2.5, 5.0, P)                                # round handguard
    for i in range(6):
        g.b(CX, 7.95, 1.1 + i * 0.75, 2.6, 2.2, 0.15, D)
    g.rail(CX, 10.3, 0.8, 5.8, w=1.2)                                  # top rail
    g.b(CX, 8.0, 5.8, 2.0, 2.6, 5.0, D)                                # upper receiver
    g.b(CX + 1.05, 9.2, 8.0, 0.4, 0.6, 0.5, M)                         # forward assist
    g.b(CX + 1.02, 8.6, 7.0, 0.06, 1.4, 1.6, P)                        # ejection port
    g.b(CX, 10.6, 6.2, 1.2, 1.0, 3.6, D)                               # carry handle
    g.b(CX, 10.2, 6.6, 0.8, 0.5, 2.8, P)
    g.b(CX, 6.2, 6.0, 2.0, 1.8, 5.6, D)                                # lower receiver
    g.b(CX, 5.2, 7.2, 2.1, 1.0, 2.2, D)                                # magwell
    g.steps(CX, 5.4, 7.4, 4, 1.5, 0.12, 1.5, 2.0, M)                   # STANAG mag
    for i in range(4):
        g.b(CX, 5.4 - 1.5 * (i + 1) + 0.6, 7.4 + 0.12 * i + 0.05, 1.6, 0.2, 1.9, D)
    g.steps(CX, 6.2, 10.6, 4, 1.15, 0.4, 1.7, 1.7, P)                  # pistol grip
    g.b(CX, 4.8, 9.2, 1.2, 0.3, 1.8, D)
    g.b(CX, 5.0, 9.6, 0.4, 1.0, 0.35, M)
    g.b(CX, 8.2, 11.4, 0.9, 0.9, 3.0, P)                               # buffer tube
    g.b(CX, 6.4, 12.6, 1.6, 3.0, 3.2, P)                               # stock
    g.b(CX, 6.0, 15.6, 1.7, 3.6, 0.4, RB)
    g.b(CX, 10.3, 11.2, 1.0, 0.4, 0.8, P)                              # charging handle
    g.anchor = (CX, 5.0, 10.8)
    return g


def scar_h():
    g = G()
    g.b(CX, 9.1, -0.5, 1.0, 1.0, 1.0, D)
    g.b(CX, 9.0, -0.9, 1.4, 1.2, 0.7, D)                               # flash hider
    g.b(CX, 7.9, 0.4, 2.4, 2.6, 5.6, TN)                               # handguard
    g.rail(CX, 10.5, 0.4, 5.9, w=1.2)
    for i in range(5):                                                 # side vent slots
        g.b(CX + 1.22, 8.5, 0.9 + i * 1.0, 0.05, 1.4, 0.4, P)
        g.b(CX - 1.22, 8.5, 0.9 + i * 1.0, 0.05, 1.4, 0.4, P)
    g.b(CX, 8.1, 5.9, 2.2, 2.7, 6.0, TN)                               # upper receiver
    g.rail(CX, 10.8, 5.9, 11.9, w=1.3)
    g.b(CX, 11.1, 1.5, 0.4, 0.8, 0.5, D)                               # flip-up front sight
    g.b(CX, 11.1, 10.8, 0.9, 0.7, 0.6, D)                              # flip-up rear sight
    g.b(CX + 1.3, 9.8, 6.8, 0.6, 0.5, 0.9, D)                          # charging handle
    g.b(CX, 5.9, 6.4, 2.1, 2.2, 5.4, TN)                               # lower
    g.steps(CX, 5.9, 7.4, 4, 1.6, 0.18, 1.7, 2.3, D)                   # 20rd magazine
    g.steps(CX, 5.9, 11.0, 4, 1.2, 0.45, 1.7, 1.7, TN)                 # grip
    g.b(CX, 4.4, 9.2, 1.3, 0.3, 1.9, D)
    g.b(CX, 4.6, 9.6, 0.4, 1.0, 0.35, M)
    g.b(CX, 6.8, 11.9, 1.6, 3.6, 3.9, TN)                              # stock
    g.b(CX, 9.6, 12.2, 1.5, 0.5, 3.0, TN)                              # cheek riser
    g.b(CX, 6.4, 15.8, 1.8, 4.2, 0.4, RB)
    g.anchor = (CX, 5.0, 11.4)
    return g


def steyr_aug():
    g = G()
    g.b(CX, 8.2, -0.2, 0.9, 0.9, 3.4, D)
    g.b(CX, 8.1, -0.6, 1.3, 1.1, 0.7, P)
    g.b(CX, 5.6, 3.0, 2.4, 4.6, 12.0, OD)                              # polymer body
    g.b(CX, 10.2, 5.0, 1.7, 1.7, 7.4, OD)                              # carry-handle optic housing
    g.cyl_z(CX, 11.0, 5.2, 6.6, 0.8, P)                                # scope tube
    g.b(CX, 10.5, 5.0, 1.1, 1.1, 0.15, GL)                             # lenses
    g.b(CX, 10.5, 12.3, 1.1, 1.1, 0.15, GL)
    g.b(CX, 3.0, 3.8, 1.3, 2.8, 1.6, OD)                               # folding foregrip
    g.b(CX, 2.7, 3.7, 1.5, 0.4, 1.8, P)
    g.b(CX, 2.8, 5.8, 1.6, 3.2, 1.6, OD)                               # pistol grip (ahead of mag)
    g.b(CX, 1.6, 8.4, 1.7, 4.2, 2.2, P)                                # translucent magazine
    for i in range(4):
        g.b(CX, 2.2 + i * 0.9, 8.5, 1.4, 0.4, 2.0, BR)
    g.b(CX, 4.0, 7.4, 1.0, 0.3, 1.0, P)                                # trigger guard
    g.b(CX, 5.6, 14.4, 1.8, 4.4, 1.0, P)                               # butt
    g.b(CX + 1.25, 8.4, 11.0, 0.5, 0.5, 0.9, M)                        # charging handle
    g.b(CX + 1.22, 7.2, 10.0, 0.06, 1.4, 1.6, P)                       # ejection port
    g.anchor = (CX, 3.5, 6.4)
    return g


def m1_garand():
    g = G()
    g.b(CX, 8.5, -0.3, 0.9, 0.9, 6.4, M)                               # barrel
    g.b(CX, 8.0, 0.6, 0.8, 0.8, 4.4, M)                                # gas cylinder
    g.b(CX, 9.4, 0.0, 0.9, 0.8, 0.5, M)                                # sight ears
    g.b(CX, 9.8, 0.1, 0.25, 0.6, 0.25, D)
    g.b(CX, 7.2, 1.6, 1.9, 1.9, 5.0, W)                                # handguard
    g.b(CX, 5.6, 3.0, 2.0, 1.6, 5.4, W)
    g.b(CX, 8.1, 6.6, 1.8, 2.3, 4.0, M)                                # receiver
    g.b(CX + 1.1, 8.3, 3.0, 0.4, 0.5, 5.4, M)                          # operating rod
    g.b(CX + 1.4, 8.4, 6.6, 0.7, 0.5, 1.0, M)                          # op-rod handle
    g.b(CX, 10.4, 6.9, 0.8, 0.45, 2.8, BR)                             # en-bloc clip
    for i in range(8):
        g.b(CX, 10.8, 7.0 + i * 0.3, 0.5, 0.25, 0.18, BR)
    g.b(CX, 10.4, 10.4, 0.8, 0.8, 0.9, D)                              # peep sight
    g.b(CX, 4.2, 8.2, 1.4, 0.35, 3.0, D)                               # trigger guard
    g.b(CX, 4.5, 9.2, 0.35, 1.0, 0.35, M)
    g.b(CX, 5.4, 6.2, 2.0, 2.7, 5.4, W)                                # stock mid
    g.b(CX, 5.6, 11.4, 1.9, 3.2, 2.2, W)                               # wrist
    g.b(CX, 4.6, 13.4, 1.9, 4.6, 2.2, WD)                              # butt
    g.b(CX, 4.8, 15.6, 2.0, 4.6, 0.4, D)
    g.b(CX, 6.0, 9.6, 2.05, 0.15, 3.4, WD)                             # grain line
    g.anchor = (CX, 5.6, 11.0)
    return g


def m249():
    g = G()
    g.b(CX, 8.4, -0.5, 1.0, 1.0, 1.0, D)
    g.b(CX, 8.3, -0.9, 1.4, 1.2, 0.7, D)
    g.b(CX, 8.2, 0.3, 1.5, 1.5, 5.4, D)                                # barrel
    g.b(CX, 7.4, 1.6, 2.8, 2.7, 4.2, P)                                # handguard
    g.vents(CX, 8.0, 2.0, 5, 2.9, 0.4, 0.75, D, 0.3)
    g.b(CX - 0.9, 6.0, 1.6, 0.3, 0.3, 3.4, M)                          # folded bipod
    g.b(CX + 0.9, 6.0, 1.6, 0.3, 0.3, 3.4, M)
    g.b(CX, 9.8, 2.6, 0.5, 1.4, 0.5, D)                                # carry handle
    g.b(CX, 11.0, 2.4, 0.7, 0.35, 2.6, D)
    g.b(CX, 6.8, 5.6, 2.6, 3.6, 5.6, P)                                # receiver
    g.b(CX, 10.4, 5.8, 2.4, 0.5, 3.6, D)                               # feed tray
    g.b(CX, 2.2, 5.8, 3.2, 3.8, 3.8, OD)                               # ammo box
    g.b(CX, 1.9, 5.8, 3.3, 0.4, 3.9, D)
    for i in range(7):                                                 # belt of rounds
        g.b(CX - 0.4, 5.6 + i * 0.7, 6.4 + (i % 2) * 0.2, 0.7, 0.5, 2.8, BR)
    g.b(CX, 10.6, 9.0, 1.2, 0.7, 1.2, D)                               # rear sight
    g.b(CX + 1.45, 8.8, 8.0, 0.6, 0.6, 1.0, D)                         # charging handle
    g.steps(CX, 6.8, 10.8, 4, 0.9, 0.4, 1.5, 1.6, P)                   # grip
    g.b(CX, 5.4, 12.0, 2.0, 4.0, 3.8, P)                               # stock
    g.b(CX, 5.0, 15.6, 2.0, 4.8, 0.4, RB)
    g.anchor = (CX, 4.5, 10.8)
    return g


def remington_870():
    g = G()
    g.b(CX, 9.2, -0.2, 1.0, 1.0, 9.4, BL)                              # barrel
    g.b(CX, 10.2, 0.3, 0.55, 0.25, 8.4, P)                             # vent rib
    g.b(CX, 10.4, 0.3, 0.3, 0.3, 0.3, OR)                              # bead sight
    g.b(CX, 8.0, 0.4, 1.0, 1.0, 8.6, BL)                               # magazine tube
    g.b(CX, 7.9, 0.2, 1.25, 1.2, 0.5, BL)                              # cap
    g.b(CX, 6.8, 3.4, 2.2, 2.3, 3.6, W)                                # pump forend
    for i in range(5):
        g.b(CX, 7.1, 3.7 + i * 0.6, 2.3, 1.7, 0.12, WD)
    g.b(CX, 7.4, 9.0, 1.8, 3.2, 3.0, BL)                               # receiver
    g.b(CX + 0.95, 8.8, 9.6, 0.1, 1.2, 1.7, P)                         # ejection port
    g.b(CX, 6.5, 10.4, 0.5, 0.4, 0.5, RD)                              # safety
    g.b(CX, 6.2, 9.4, 1.2, 0.3, 1.8, D)                                # trigger guard
    g.b(CX, 6.3, 10.0, 0.3, 0.9, 0.3, M)
    g.b(CX, 5.4, 11.6, 1.7, 2.0, 1.1, W)                               # wrist
    g.b(CX, 6.2, 12.0, 1.8, 3.6, 3.0, W)                               # stock
    g.b(CX, 4.4, 14.4, 1.8, 5.4, 1.2, WD)
    g.b(CX, 4.2, 15.6, 1.9, 5.8, 0.4, RB)
    g.anchor = (CX, 6.0, 11.0)
    return g


def spas12():
    g = G()
    g.b(CX, 9.0, -0.3, 1.0, 1.0, 7.0, D)
    g.b(CX, 8.9, -0.7, 1.4, 1.2, 0.7, D)
    g.b(CX, 7.8, 0.4, 1.0, 1.0, 7.0, D)
    g.b(CX, 6.6, 3.3, 2.4, 2.4, 4.0, P)                                # ribbed pump
    for i in range(6):
        g.b(CX, 6.8, 3.5 + i * 0.6, 2.5, 2.0, 0.14, D)
    g.b(CX, 6.6, 8.0, 2.0, 3.6, 4.0, D)                                # receiver
    g.b(CX, 10.2, 7.6, 0.8, 0.6, 3.6, D)                               # carry handle
    g.b(CX, 10.8, 7.6, 0.8, 0.3, 3.6, M)
    g.b(CX + 1.05, 8.8, 9.0, 0.1, 1.2, 1.6, P)
    g.b(CX, 4.8, 9.0, 1.2, 0.3, 2.0, D)
    g.b(CX, 5.0, 9.6, 0.3, 1.0, 0.3, M)
    g.steps(CX, 6.6, 11.0, 4, 1.2, 0.4, 1.6, 1.7, P)                   # pistol grip
    for i in range(4):                                                 # skeleton folding stock
        g.b(CX, 7.8 - i * 0.2, 12.0 + i * 0.9, 0.4, 0.4, 0.9, M)
    g.b(CX, 4.0, 15.4, 0.5, 4.2, 0.6, M)
    g.b(CX, 3.6, 15.4, 1.8, 0.5, 0.7, RB)                              # hooked arm brace
    g.anchor = (CX, 5.0, 11.4)
    return g


def aa12():
    g = G()
    g.b(CX, 8.6, -0.3, 1.2, 1.2, 4.0, D)
    g.b(CX, 8.5, -0.7, 1.7, 1.4, 0.7, D)
    g.b(CX, 6.6, 3.4, 2.8, 3.8, 8.0, P)                                # boxy receiver
    g.vents(CX, 7.0, 3.8, 6, 2.9, 0.4, 1.0, D, 0.35)
    g.b(CX, 3.4, 1.6, 1.3, 3.6, 1.4, P)                                # foregrip
    g.b(CX, 4.8, 1.2, 1.6, 0.5, 2.0, D)
    g.cyl_x(CX, 3.2, 8.0, 2.0, 3.4, D)                                 # 32-round drum
    g.cyl_x(CX, 3.2, 8.0, 2.15, 2.4, M)
    g.cyl_x(CX, 3.2, 8.0, 2.2, 1.0, S)
    g.b(CX, 10.4, 4.0, 0.9, 0.9, 5.0, D)                               # carry handle
    g.b(CX, 11.2, 4.0, 1.0, 0.3, 5.0, M)
    g.b(CX, 10.4, 10.6, 1.0, 0.7, 1.0, D)                              # rear sight
    g.b(CX, 9.4, 11.6, 1.0, 1.0, 3.0, D)                               # recoil spring tube
    g.steps(CX, 6.6, 8.8, 4, 1.2, 0.4, 1.6, 1.8, P)
    g.b(CX, 5.8, 12.0, 2.0, 3.8, 3.6, P)
    g.b(CX, 5.4, 15.5, 2.0, 4.4, 0.5, RB)
    g.anchor = (CX, 5.0, 9.4)
    return g


def remington_700():
    g = G()
    g.b(CX, 8.9, -0.3, 1.2, 1.2, 8.4, BL)                              # heavy barrel
    g.b(CX, 8.8, -0.9, 1.7, 1.4, 1.1, D)                               # muzzle brake
    for i in range(3):
        g.b(CX, 9.2, -0.8 + i * 0.35, 1.8, 0.5, 0.12, P)
    g.b(CX, 6.4, 3.0, 1.9, 2.0, 6.4, OD)                               # forend
    g.b(CX, 7.6, 8.2, 1.7, 2.2, 3.2, BL)                               # action
    g.b(CX + 1.1, 8.3, 10.2, 1.4, 0.5, 0.5, S)                         # bolt handle
    g.b(CX + 2.2, 8.0, 10.1, 0.7, 0.8, 0.7, S)
    g.b(CX, 4.8, 8.6, 1.4, 0.4, 2.8, D)
    g.b(CX, 5.2, 9.6, 0.3, 1.0, 0.3, M)
    g.b(CX, 4.8, 8.2, 1.5, 1.4, 2.0, D)                                # magazine floorplate
    # scope
    g.cyl_z(CX, 11.6, 4.2, 6.6, 0.8, P)
    g.cyl_z(CX, 11.6, 3.4, 1.2, 1.1, P)
    g.cyl_z(CX, 11.6, 10.6, 1.4, 1.0, P)
    g.b(CX, 11.0, 3.3, 1.2, 1.2, 0.1, GL)
    g.b(CX, 11.0, 12.0, 0.9, 0.9, 0.1, GL)
    g.b(CX, 12.5, 6.4, 0.9, 0.7, 0.9, D)
    g.b(CX + 0.9, 11.3, 7.4, 0.7, 0.7, 0.7, D)
    g.b(CX, 9.6, 5.0, 1.0, 1.6, 0.8, D)                                # rings
    g.b(CX, 9.6, 9.4, 1.0, 1.6, 0.8, D)
    g.b(CX - 0.9, 5.2, 3.6, 0.3, 0.3, 3.4, D)                          # folded bipod
    g.b(CX + 0.9, 5.2, 3.6, 0.3, 0.3, 3.4, D)
    g.b(CX, 6.0, 11.4, 1.8, 3.2, 4.6, OD)                              # stock
    g.b(CX, 8.8, 12.4, 1.7, 0.9, 3.2, OD)                              # cheek
    g.b(CX, 5.6, 15.6, 1.9, 4.2, 0.4, RB)
    g.anchor = (CX, 6.0, 10.4)
    return g


def barrett_m82():
    g = G()
    g.b(CX, 8.8, -0.2, 1.8, 1.8, 7.4, D)                               # barrel
    g.b(CX, 8.5, -1.0, 2.5, 2.4, 2.0, D)                               # muzzle brake
    for i in range(3):
        g.b(CX, 8.7, -0.8 + i * 0.6, 2.6, 0.4, 0.3, P)
    for i in range(5):                                                 # barrel flutes
        g.b(CX, 9.0, 2.4 + i * 1.0, 1.9, 1.4, 0.18, M)
    g.b(CX, 7.4, 7.2, 2.4, 3.4, 5.8, D)                                # receiver
    g.rail(CX, 10.8, 3.0, 12.6, w=1.5)
    g.b(CX, 4.0, 8.4, 1.9, 3.4, 3.4, D)                                # box magazine
    g.b(CX, 3.7, 8.3, 2.0, 0.4, 3.6, P)
    g.cyl_z(CX, 12.2, 5.0, 7.2, 0.9, P)                                # scope
    g.cyl_z(CX, 12.2, 4.2, 1.2, 1.2, P)
    g.cyl_z(CX, 12.2, 11.8, 1.4, 1.1, P)
    g.b(CX, 11.6, 4.1, 1.3, 1.3, 0.1, GL)
    g.b(CX, 13.2, 7.6, 1.0, 0.7, 1.0, D)
    g.b(CX - 1.0, 6.0, 1.6, 0.5, 0.5, 4.4, D)                          # folded bipod
    g.b(CX + 1.0, 6.0, 1.6, 0.5, 0.5, 4.4, D)
    g.b(CX + 1.6, 9.0, 8.2, 1.0, 0.6, 0.9, M)                          # charging handle
    g.steps(CX, 7.4, 11.0, 4, 1.1, 0.4, 1.8, 1.8, P)                   # grip
    g.b(CX, 4.8, 9.6, 1.3, 0.3, 2.0, D)
    g.b(CX, 6.6, 13.0, 1.9, 3.8, 3.0, P)                               # stock
    g.b(CX, 6.2, 15.6, 2.0, 4.6, 0.4, RB)
    g.b(CX, 3.4, 14.4, 0.8, 3.0, 0.8, D)                               # monopod
    g.anchor = (CX, 5.0, 11.2)
    return g


def dragunov_svd():
    g = G()
    g.b(CX, 9.6, -0.2, 1.0, 1.0, 5.4, D)
    g.b(CX, 9.5, -0.6, 1.4, 1.2, 0.8, D)                               # flash hider
    g.b(CX, 10.3, 1.2, 0.4, 0.8, 0.4, D)
    g.b(CX, 8.2, 2.6, 1.8, 2.2, 3.6, W)                                # handguard
    g.b(CX, 7.8, 6.8, 1.9, 2.7, 4.4, D)                                # receiver
    g.b(CX, 10.6, 6.9, 1.7, 0.2, 4.2, D)
    g.b(CX, 10.8, 6.2, 0.8, 0.6, 1.2, D)                               # rear sight
    g.cyl_z(CX, 11.8, 5.6, 5.6, 0.75, P)                               # PSO-1 scope
    g.cyl_z(CX, 11.8, 5.0, 1.0, 1.0, P)
    g.cyl_z(CX, 11.8, 10.4, 1.2, 0.9, P)
    g.b(CX, 11.2, 4.9, 1.2, 1.2, 0.1, GL)
    g.b(CX, 9.6, 6.4, 1.0, 1.4, 0.8, D)
    g.steps(CX, 7.8, 8.0, 5, 1.2, -0.3, 1.5, 2.6, D)                   # curved magazine
    g.b(CX, 6.0, 10.0, 1.3, 0.3, 1.8, D)
    g.b(CX, 6.1, 10.4, 0.3, 1.0, 0.3, M)
    # thumbhole skeleton stock
    g.b(CX, 7.8, 11.2, 1.8, 1.6, 4.4, W)                               # cheek / top
    g.b(CX, 4.4, 11.4, 1.8, 1.0, 3.0, W)                               # bottom bar
    g.b(CX, 4.4, 10.6, 1.8, 3.4, 1.0, W)                               # grip
    g.b(CX, 4.4, 14.2, 1.8, 5.0, 1.4, W)                               # rear
    g.b(CX, 4.6, 15.6, 1.9, 4.8, 0.4, RB)
    g.anchor = (CX, 5.5, 10.8)
    return g
