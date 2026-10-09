package com.sharinganmod.client;

import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class ClientOverlay {
    private ClientOverlay() {}

    private static final String[] KEYS = {"\\", "Z", "X", "C"};

    /** Tela de visao: tinta do Sharingan, desgaste e cegueira. */
    public static void renderVision(GuiGraphics gg, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        SharinganData d = ClientData.DATA;
        int w = gg.guiWidth();
        int h = gg.guiHeight();

        if (d.eyesActive()) gg.fill(0, 0, w, h, 0x18FF0000);
        if (d.susanooOn) gg.fill(0, 0, w, h, 0x206040FF);

        if (d.blind) {
            // Cego: escuridao total. Com o Sharingan ativo, restam so silhuetas escuras do mundo.
            int alpha = d.eyesActive() ? 0xD2 : 0xFF;
            gg.fill(0, 0, w, h, alpha << 24);
        } else if (d.strain > 60) {
            int a = (int) ((d.strain - 60) / 40f * 0x90);
            gg.fill(0, 0, w, h, (a << 24) | 0x300000);
        }
    }

    public static void renderHud(GuiGraphics gg, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        SharinganData d = ClientData.DATA;
        if (d.stage <= 0) return;
        Font f = mc.font;
        int x = 8;
        int y = gg.guiHeight() - 20;

        // desenha de baixo para cima
        if (d.stage >= 4) {
            for (int i = 3; i >= 0; i--) {
                String name = Skills.NAMES[d.type][i];
                String state = d.cooldowns[i] > 0 ? (d.cooldowns[i] / 20 + 1) + "s" : "pronto";
                int col = d.cooldowns[i] > 0 ? 0xAAAAAA : 0x55FF55;
                if (i == 3 && d.susanooOn) {
                    state = "ATIVO";
                    col = 0x8888FF;
                }
                gg.drawString(f, "[" + KEYS[i] + "] " + name + ": " + state, x, y, col);
                y -= 10;
            }
            if (d.stage < 5) {
                int barW = 100;
                gg.fill(x, y, x + barW, y + 5, 0xFF222222);
                gg.fill(x, y, x + (int) (barW * d.strain / 100f), y + 5, 0xFFCC1111);
                gg.drawString(f, "Desgaste ocular", x + barW + 4, y - 2, 0xFF8888);
                y -= 10;
            }
        } else if (d.stage < 3) {
            int barW = 100;
            gg.fill(x, y, x + barW, y + 4, 0xFF222222);
            gg.fill(x, y, x + (int) (barW * d.xp / (float) Math.max(1, d.xpNeeded())), y + 4, 0xFFDD2222);
            y -= 10;
        }
        String title = Skills.stageName(d) + (d.eyesActive() ? " [ATIVO]" : " [off]") + (d.blind ? " - CEGO" : "");
        gg.drawString(f, title, x, y, d.eyesActive() ? 0xFF4444 : 0x999999);
    }
}
