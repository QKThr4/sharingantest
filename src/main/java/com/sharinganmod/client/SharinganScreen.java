package com.sharinganmod.client;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import com.sharinganmod.net.ActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/** Aba especial do Sharingan (tecla J): slot do olho equipado e acoes. */
public class SharinganScreen extends Screen {
    private Button equip;
    private Button extract;
    private Button share;

    public SharinganScreen() {
        super(Component.literal("Aba Sharingan"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 + 40;
        equip = addRenderableWidget(Button.builder(Component.literal("Equipar"), b -> send(8))
                .bounds(cx - 100, y, 200, 20).build());
        extract = addRenderableWidget(Button.builder(Component.literal("Extrair olho Mangekyou"), b -> send(9))
                .bounds(cx - 100, y + 24, 200, 20).build());
        share = addRenderableWidget(Button.builder(Component.literal("Partilhar olho de Obito (metade)"), b -> send(7))
                .bounds(cx - 100, y + 48, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(cx - 100, y + 72, 200, 20).build());
        refreshButtons();
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new ActionPayload(action));
    }

    private void refreshButtons() {
        SharinganData d = ClientData.DATA;
        if (equip == null) return;
        equip.setMessage(Component.literal(d.equipped ? "Remover Sharingan" : "Equipar Sharingan"));
        equip.active = d.stage > 0;
        extract.active = d.stage == 4 && (d.type == 1 || d.type == 2 || d.type == 4);
        share.active = d.stage == 4 && d.type == 3 && !d.halfEye && !d.gaveEye;
    }

    @Override
    public void tick() {
        refreshButtons();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeys.SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String iconKey(SharinganData d) {
        if (d.stage <= 3) return String.valueOf(Math.max(1, d.stage));
        if (d.stage == 5) return "eterno";
        return switch (d.type) {
            case 1 -> "itachi";
            case 2 -> "sasuke";
            case 3 -> "obito";
            default -> "shisui";
        };
    }

    @Override
    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gg, mouseX, mouseY, partialTick);
        SharinganData d = ClientData.DATA;
        int cx = this.width / 2;
        int cy = this.height / 2;

        gg.fill(cx - 120, cy - 120, cx + 120, cy + 138, 0xC0101010);
        gg.fill(cx - 120, cy - 120, cx + 120, cy - 118, 0xFFAA1111);
        gg.drawCenteredString(this.font, "Aba Sharingan", cx, cy - 114, 0xFFFFFF);

        // slot do olho
        gg.fill(cx - 38, cy - 104, cx + 38, cy - 28, 0xFF555555);
        gg.fill(cx - 36, cy - 102, cx + 36, cy - 30, 0xFF1A1A1A);
        if (d.stage > 0) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID,
                    "textures/gui/eye_" + iconKey(d) + ".png");
            if (!d.equipped) gg.setColor(0.35f, 0.35f, 0.35f, 1f);
            gg.blit(tex, cx - 32, cy - 98, 64, 64, 0, 0, 64, 64, 64, 64);
            gg.setColor(1f, 1f, 1f, 1f);
        }

        int y = cy - 22;
        gg.drawCenteredString(this.font, Skills.stageName(d) + (d.equipped || d.stage == 0 ? "" : " (removido)"), cx, y, 0xFF5555);
        y += 11;
        if (d.stage == 0) {
            gg.drawCenteredString(this.font, "Sobreviva a um golpe quase fatal para despertar.", cx, y, 0xAAAAAA);
        } else if (d.stage < 3) {
            gg.drawCenteredString(this.font, "XP: " + d.xp + " / " + d.xpNeeded() + " (ative com V e ganhe xp)", cx, y, 0xAAAAAA);
        } else if (d.stage == 3) {
            gg.drawCenteredString(this.font, "Mate um mob domesticado para despertar o Mangekyou.", cx, y, 0xAAAAAA);
        } else {
            gg.drawCenteredString(this.font, d.stage == 5 ? "Sem desgaste: visao preservada." : "Desgaste ocular: " + (int) d.strain + "%", cx, y, 0xAAAAAA);
            y += 11;
            if (d.halfEye) gg.drawCenteredString(this.font, "Meio olho: recargas +50%, desgaste -50%.", cx, y, 0xBB88FF);
        }
        if (d.blind) {
            gg.drawCenteredString(this.font, "CEGUEIRA PERMANENTE (use Sharingan para ver silhuetas)", cx, cy + 24, 0xFF2222);
        }
    }
}
