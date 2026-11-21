package com.tougen.client;

import com.tougen.data.OniAbility;
import com.tougen.data.OniData;
import com.tougen.entity.ShikiGunEntity;
import com.tougen.gun.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Oni HUD: blood PERCENT (no bar), school rank, ability info (bottom left) and gun ammo (bottom right). */
public class BloodMeterOverlay implements IGuiOverlay {

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        Font font = mc.font;

        if (ClientHooks.classId == OniData.ONI) drawOni(g, font, mc, h);
        ItemStack held = mc.player.getMainHandItem();
        if (held.getItem() instanceof GunItem gun) drawAmmo(g, font, mc, held, gun, w, h);
    }

    private void drawOni(GuiGraphics g, Font font, Minecraft mc, int h) {
        int x = 12;
        int y = h - 62;
        int pct = Math.round(ClientHooks.blood);
        boolean empty = ClientHooks.blood <= 0.01f;
        int bloodColor = empty ? 0xFFFF4040 : (pct >= 100 ? 0xFFFF5050 : 0xFFE03030);

        g.pose().pushPose();
        g.pose().scale(2f, 2f, 1f);
        g.drawString(font, "BLOOD " + pct + "%", (int) (x / 2f), (int) (y / 2f), bloodColor, true);
        g.pose().popPose();

        g.drawString(font, OniAbility.rankName(ClientHooks.rank), x, y + 20, 0xFFD6A8A8, true);

        OniAbility[] all = OniAbility.values();
        OniAbility sel = all[Math.max(0, Math.min(all.length - 1, ClientHooks.selected))];
        int line = y + 31;
        if (ClientHooks.gunKind > 0) {
            int ammo = findAmmo(mc);
            String s = "[V] dismiss  LMB fire  RMB aim" + (ammo >= 0 ? "   ammo " + ammo : "");
            g.drawString(font, s, x, line, 0xFFFF7070, true);
        } else {
            boolean unlocked = sel.unlockedAt(ClientHooks.rank);
            float cost = sel.cost(ClientHooks.rank);
            boolean enough = ClientHooks.blood + 0.001f >= cost;
            String s = unlocked
                    ? "[V] " + sel.title + " - " + Math.round(cost) + "%   [B] switch"
                    : sel.title + " (unlocks: " + OniAbility.rankName(sel.unlockRank) + ")   [B] switch";
            g.drawString(font, s, x, line, unlocked && enough ? 0xFFFF9090 : 0xFF886666, true);
        }
        if (ClientHooks.legsActive()) {
            long left = (ClientHooks.legEnd - mc.level.getGameTime()) / 20;
            g.drawString(font, "Blood Legs " + left + "s  (walk into walls)", x, line + 11, 0xFFFFB0B0, true);
        }
        if (empty) {
            g.drawString(font, "OUT OF BLOOD - recovering", x, line + (ClientHooks.legsActive() ? 22 : 11), 0xFFFF4040, true);
        }
    }

    private int findAmmo(Minecraft mc) {
        if (mc.level == null || mc.player == null) return -1;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof ShikiGunEntity gun && gun.getOwnerEntityId() == mc.player.getId()) return gun.getAmmo();
        }
        return -1;
    }

    private void drawAmmo(GuiGraphics g, Font font, Minecraft mc, ItemStack held, GunItem gun, int w, int h) {
        int ammo = GunItem.getAmmo(held);
        boolean reloading = GunItem.isReloading(held);
        String main = ammo + " / " + gun.spec.magSize;
        int color = ammo == 0 ? 0xFFFF4040 : 0xFFFFFFFF;

        g.pose().pushPose();
        g.pose().scale(2f, 2f, 1f);
        g.drawString(font, main, (int) ((w - 14) / 2f - font.width(main)), (int) ((h - 46) / 2f), color, true);
        g.pose().popPose();

        String sub = reloading ? "RELOADING..." : "x" + GunItem.countBullets(mc.player) + " bullets  [R]";
        g.drawString(font, sub, w - 14 - font.width(sub), h - 22, reloading ? 0xFFFFC040 : 0xFFAAAAAA, true);
        g.drawString(font, gun.spec.displayName, w - 14 - font.width(gun.spec.displayName), h - 62, 0xFFCCCCCC, true);
    }
}
