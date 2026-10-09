package com.sharinganmod.data;

import com.sharinganmod.registry.ModRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Estado do Sharingan de um jogador (salvo no jogador e copiado ao morrer). */
public class SharinganData implements INBTSerializable<CompoundTag> {
    /** "Ticks" que o Sharingan tira dos inimigos por nivel (0..5). */
    public static final int[] SLOW_TICKS = {0, 2, 3, 4, 5, 6};
    public static final int XP_TO_2 = 200;
    public static final int XP_TO_3 = 600;

    /** 0 nada, 1-3 tomoe, 4 mangekyou, 5 mangekyou eterno */
    public int stage;
    /** 0 nenhum, 1 Itachi, 2 Sasuke, 3 Obito, 4 Shisui, 5 Eterno */
    public int type;
    public int xp;
    public boolean equipped = true;
    public boolean sharinganOn;
    public boolean mangekyouOn;
    public boolean susanooOn;
    public boolean blind;
    public boolean halfEye;
    public boolean gaveEye;
    public float strain;
    public int susanooTicks;
    public int intangibleTicks;
    public int suctionTicks;
    public int[] cooldowns = new int[4];

    public static SharinganData get(Player p) {
        return p.getData(ModRegistries.SHARINGAN);
    }

    /** Nivel efetivo dos poderes (0 = desligado). */
    public int level() {
        if (!sharinganOn || !equipped || stage <= 0) return 0;
        if (stage <= 3) return stage;
        return mangekyouOn ? stage : 3;
    }

    public boolean eyesActive() {
        return sharinganOn && equipped && stage > 0;
    }

    public int xpNeeded() {
        return stage == 1 ? XP_TO_2 : stage == 2 ? XP_TO_3 : 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag t = new CompoundTag();
        t.putInt("stage", stage);
        t.putInt("type", type);
        t.putInt("xp", xp);
        t.putBoolean("equipped", equipped);
        t.putBoolean("on", sharinganOn);
        t.putBoolean("mk", mangekyouOn);
        t.putBoolean("sus", susanooOn);
        t.putBoolean("blind", blind);
        t.putBoolean("half", halfEye);
        t.putBoolean("gave", gaveEye);
        t.putFloat("strain", strain);
        t.putInt("susT", susanooTicks);
        t.putInt("intT", intangibleTicks);
        t.putInt("sucT", suctionTicks);
        t.putIntArray("cd", cooldowns);
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag t) {
        stage = t.getInt("stage");
        type = t.getInt("type");
        xp = t.getInt("xp");
        equipped = !t.contains("equipped") || t.getBoolean("equipped");
        sharinganOn = t.getBoolean("on");
        mangekyouOn = t.getBoolean("mk");
        susanooOn = t.getBoolean("sus");
        blind = t.getBoolean("blind");
        halfEye = t.getBoolean("half");
        gaveEye = t.getBoolean("gave");
        strain = t.getFloat("strain");
        susanooTicks = t.getInt("susT");
        intangibleTicks = t.getInt("intT");
        suctionTicks = t.getInt("sucT");
        int[] a = t.getIntArray("cd");
        cooldowns = new int[4];
        for (int i = 0; i < 4 && i < a.length; i++) cooldowns[i] = a[i];
    }
}
