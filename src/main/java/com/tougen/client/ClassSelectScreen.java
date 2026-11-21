package com.tougen.client;

import com.tougen.data.OniData;
import com.tougen.net.ClassPacket;
import com.tougen.net.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * "Do you want to be an Oni or a Momotaro?" - a red panel with a dark outline holding two squares.
 * The Oni square is drawn in Tougen Anki's demon-blood style (blood-black red, dripping, horned mask);
 * the Momotaro square in the Momotaro style (peach pink, peach emblem, blade).
 */
public class ClassSelectScreen extends Screen {
    private static final int SQ = 124;      // square size
    private static final int GAP = 44;

    private int oniX, momoX, sqY;

    public ClassSelectScreen() {
        super(Component.literal("Choose your path"));
    }

    @Override
    protected void init() {
        oniX = width / 2 - GAP / 2 - SQ;
        momoX = width / 2 + GAP / 2;
        sqY = height / 2 - SQ / 2 + 4;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (inside(mx, my, oniX)) {
            choose(OniData.ONI);
            return true;
        }
        if (inside(mx, my, momoX)) {
            choose(OniData.MOMOTARO);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean inside(double mx, double my, int x) {
        return mx >= x && mx < x + SQ && my >= sqY && my < sqY + SQ;
    }

    private void choose(int id) {
        Net.CHANNEL.sendToServer(new ClassPacket(id));
        ClientHooks.classId = id;
        onClose();
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // dim world + red vignette
        g.fillGradient(0, 0, width, height, 0xE0100000, 0xF0380000);

        int pw = SQ * 2 + GAP + 64, ph = SQ + 124;
        int px = width / 2 - pw / 2, py = height / 2 - ph / 2 + 2;
        drawPanel(g, px, py, pw, ph);

        // title
        g.pose().pushPose();
        g.pose().scale(2f, 2f, 1f);
        g.drawCenteredString(font, "CHOOSE YOUR PATH", width / 4, (py + 12) / 2, 0xFFFFE8E8);
        g.pose().popPose();
        g.drawCenteredString(font, "Oni  or  Momotaro?", width / 2, py + 36, 0xFFFFB8B8);

        boolean hoverOni = inside(mx, my, oniX);
        boolean hoverMomo = inside(mx, my, momoX);

        drawOni(g, oniX, sqY, hoverOni);
        drawMomotaro(g, momoX, sqY, hoverMomo);

        // labels
        int ly = sqY + SQ + 8;
        label(g, "ONI", "Demon blood - Shiki's guns", oniX, ly, 0xFFFF4A4A, ClientHooks.classId == OniData.ONI);
        label(g, "MOMOTARO", "Peach blade - powers coming soon", momoX, ly, 0xFFFFC2C8, ClientHooks.classId == OniData.MOMOTARO);

        g.drawCenteredString(font, "Press G any time to change your path", width / 2, py + ph - 14, 0xFFC08080);
    }

    private void label(GuiGraphics g, String big, String small, int x, int y, int color, boolean current) {
        g.drawCenteredString(font, big + (current ? "  (chosen)" : ""), x + SQ / 2, y, color);
        g.drawCenteredString(font, small, x + SQ / 2, y + 12, 0xFFD6A8A8);
    }

    /** Red panel with a dark outline. */
    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 4, y - 4, x + w + 4, y + h + 4, 0xFF050000);          // outer dark outline
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFF2A0404);          // inner dark rim
        g.fillGradient(x, y, x + w, y + h, 0xFFC41818, 0xFF6A0808);      // red body
        g.fill(x + 3, y + 3, x + w - 3, y + 4, 0x55FFFFFF);              // top sheen
        g.fill(x + 3, y + 3, x + 4, y + h - 3, 0x33FFFFFF);
        // corner blood drips
        for (int i = 0; i < 9; i++) {
            int dx = x + 18 + i * (w - 36) / 8;
            int len = 6 + (i * 37 % 17);
            g.fill(dx, y, dx + 3, y + len, 0xFF2A0000);
            circle(g, dx + 1, y + len, 2, 0xFF2A0000);
        }
    }

    /** Dark outline + coloured inner border around a square; brighter on hover. */
    private void frame(GuiGraphics g, int x, int y, boolean hover, int accent) {
        if (hover) {
            float pulse = 0.5f + 0.5f * Mth.sin(System.currentTimeMillis() / 160f);
            int a = (int) (70 + 80 * pulse);
            g.fill(x - 8, y - 8, x + SQ + 8, y + SQ + 8, (a << 24) | 0xFF2020);
        }
        g.fill(x - 4, y - 4, x + SQ + 4, y + SQ + 4, 0xFF050000);
        g.fill(x - 2, y - 2, x + SQ + 2, y + SQ + 2, hover ? 0xFFFF6060 : accent);
    }

    // ------------------------------------------------------------------ Oni (demon blood)

    private void drawOni(GuiGraphics g, int x, int y, boolean hover) {
        frame(g, x, y, hover, 0xFF7A0A0A);
        g.fillGradient(x, y, x + SQ, y + SQ, 0xFF2A0404, 0xFF8A0C0C);

        int cx = x + SQ / 2;

        // blood splatter blobs
        int[][] blobs = {{16, 96, 9}, {30, 108, 5}, {100, 90, 11}, {108, 104, 6}, {22, 60, 4}, {104, 56, 5}, {60, 112, 7}};
        for (int[] b : blobs) circle(g, x + b[0], y + b[1], b[2], 0xCC5A0000);

        // glow behind the head
        circle(g, cx, y + 66, 44, 0x22FF2020);
        circle(g, cx, y + 66, 36, 0x22FF2020);

        // horns (bone coloured, curved tips)
        tri(g, cx - 30, y + 54, cx - 14, y + 44, cx - 36, y + 12, 0xFFE8DCC0);
        tri(g, cx + 30, y + 54, cx + 14, y + 44, cx + 36, y + 12, 0xFFE8DCC0);
        tri(g, cx - 28, y + 52, cx - 18, y + 46, cx - 33, y + 20, 0xFFB8A888);
        tri(g, cx + 28, y + 52, cx + 18, y + 46, cx + 33, y + 20, 0xFFB8A888);

        // head / mask
        circle(g, cx, y + 70, 28, 0xFF140202);
        circle(g, cx, y + 68, 26, 0xFF3A0808);
        g.fillGradient(cx - 22, y + 56, cx + 22, y + 66, 0x00000000, 0x55000000);

        // slanted glowing eyes
        tri(g, cx - 20, y + 60, cx - 4, y + 66, cx - 20, y + 68, 0xFFFF3A3A);
        tri(g, cx + 20, y + 60, cx + 4, y + 66, cx + 20, y + 68, 0xFFFF3A3A);
        tri(g, cx - 17, y + 63, cx - 8, y + 66, cx - 17, y + 67, 0xFFFFE0A0);
        tri(g, cx + 17, y + 63, cx + 8, y + 66, cx + 17, y + 67, 0xFFFFE0A0);

        // jagged grin + fangs
        g.fill(cx - 17, y + 80, cx + 17, y + 85, 0xFF0A0000);
        for (int i = -3; i <= 3; i++) {
            tri(g, cx + i * 5 - 2, y + 80, cx + i * 5 + 2, y + 80, cx + i * 5, y + 86, 0xFFF4F0E0);
        }
        tri(g, cx - 12, y + 80, cx - 7, y + 80, cx - 10, y + 92, 0xFFFFFFFF);
        tri(g, cx + 12, y + 80, cx + 7, y + 80, cx + 10, y + 92, 0xFFFFFFFF);

        // dripping blood from the top edge
        int[] xs = {10, 26, 44, 64, 82, 100, 114};
        int[] ls = {30, 56, 24, 66, 38, 58, 20};
        for (int i = 0; i < xs.length; i++) {
            g.fill(x + xs[i], y, x + xs[i] + 4, y + ls[i], 0xFFC41414);
            g.fill(x + xs[i] + 1, y, x + xs[i] + 2, y + ls[i], 0xFFFF5050);
            circle(g, x + xs[i] + 2, y + ls[i], 3, 0xFFC41414);
        }
    }

    // ------------------------------------------------------------------ Momotaro (peach)

    private void drawMomotaro(GuiGraphics g, int x, int y, boolean hover) {
        frame(g, x, y, hover, 0xFFE59AA0);
        g.fillGradient(x, y, x + SQ, y + SQ, 0xFFFFF0E6, 0xFFFFB3B0);

        int cx = x + SQ / 2;

        // soft rays
        for (int i = 0; i < 7; i++) {
            int rx = x + 8 + i * 17;
            g.fillGradient(rx, y, rx + 6, y + SQ, 0x22FFFFFF, 0x00FFFFFF);
        }

        // blade behind the peach
        for (int t = 0; t < 100; t++) {
            int bx = x + 14 + t;
            int by = y + SQ - 16 - t;
            g.fill(bx, by, bx + 4, by + 4, t < 12 ? 0xFFC9A227 : 0xFFE9EAF2);
            if (t >= 12) g.fill(bx + 1, by + 1, bx + 2, by + 2, 0xFFFFFFFF);
        }
        g.fill(x + 24, y + SQ - 36, x + 40, y + SQ - 32, 0xFFC9A227); // tsuba

        // the peach
        circle(g, cx, y + 70, 34, 0xFFE07880);
        circle(g, cx, y + 68, 32, 0xFFFF9AA2);
        circle(g, cx - 6, y + 60, 20, 0xFFFFB6BC);
        circle(g, cx - 12, y + 52, 8, 0xFFFFE0E0);
        // cleft
        for (int i = 0; i < 52; i++) {
            int off = (int) (Math.sin(i / 8.0) * 3);
            g.fill(cx + 8 + off, y + 40 + i, cx + 10 + off, y + 41 + i, 0xFFD8606C);
        }
        // leaf + stem
        g.fill(cx - 1, y + 28, cx + 2, y + 40, 0xFF6B4A2A);
        tri(g, cx + 2, y + 36, cx + 28, y + 24, cx + 14, y + 42, 0xFF4CAF50);
        tri(g, cx + 2, y + 36, cx + 22, y + 28, cx + 12, y + 39, 0xFF7FD97F);

        // gold sparkles
        sparkle(g, x + 18, y + 22, 0xFFFFD86B);
        sparkle(g, x + SQ - 20, y + 34, 0xFFFFD86B);
        sparkle(g, x + SQ - 30, y + SQ - 26, 0xFFFFFFFF);
        sparkle(g, x + 26, y + SQ - 60, 0xFFFFFFFF);

        // bottom ribbon
        g.fill(x, y + SQ - 10, x + SQ, y + SQ, 0xFFB03848);
        g.fill(x, y + SQ - 10, x + SQ, y + SQ - 9, 0xFFFFD86B);
    }

    // ------------------------------------------------------------------ helpers

    private static void sparkle(GuiGraphics g, int cx, int cy, int c) {
        g.fill(cx - 5, cy, cx + 6, cy + 1, c);
        g.fill(cx, cy - 5, cx + 1, cy + 6, c);
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, c & 0x88FFFFFF | 0x88000000);
    }

    private static void circle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt((double) r * r - (double) dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static void tri(GuiGraphics g, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));
        int[][] edges = {{x1, y1, x2, y2}, {x2, y2, x3, y3}, {x3, y3, x1, y1}};
        for (int y = minY; y <= maxY; y++) {
            float xl = Float.MAX_VALUE, xr = -Float.MAX_VALUE;
            for (int[] e : edges) {
                int ya = e[1], yb = e[3];
                if (ya == yb || y < Math.min(ya, yb) || y > Math.max(ya, yb)) continue;
                float t = (y - ya) / (float) (yb - ya);
                float xx = e[0] + t * (e[2] - e[0]);
                xl = Math.min(xl, xx);
                xr = Math.max(xr, xx);
            }
            if (xl <= xr) g.fill(Math.round(xl), y, Math.round(xr) + 1, y + 1, color);
        }
    }
}
