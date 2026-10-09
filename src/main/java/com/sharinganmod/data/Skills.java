package com.sharinganmod.data;

/** Tabelas de nomes, recargas (ticks) e desgaste dos olhos por habilidade. */
public final class Skills {
    private Skills() {}

    public static final String[] TYPE_NAMES = {"Nenhum", "Itachi", "Sasuke", "Obito", "Shisui", "Eterno"};

    public static final String[][] NAMES = {
            {"", "", "", ""},
            {"Tsukuyomi", "Amaterasu", "Karasu Bunshin", "Susanoo"},
            {"Chidori Corrente", "Enton: Kagutsuchi", "Kirin", "Susanoo"},
            {"Kamui: Intangibilidade", "Kamui: Teletransporte", "Kamui: Succao", "Susanoo"},
            {"Kotoamatsukami", "Shunshin", "Olhar Revelador", "Susanoo"},
            {"Tsukuyomi", "Amaterasu", "Kirin", "Susanoo Perfeito"}
    };

    public static final int[][] COOLDOWN = {
            {0, 0, 0, 0},
            {600, 400, 500, 0},
            {200, 450, 900, 0},
            {600, 300, 700, 0},
            {1200, 100, 500, 0},
            {600, 400, 900, 0}
    };

    public static final float[][] STRAIN = {
            {0, 0, 0, 0},
            {12, 10, 6, 0},
            {8, 12, 18, 0},
            {6, 8, 14, 0},
            {15, 4, 6, 0},
            {0, 0, 0, 0}
    };

    public static String stageName(SharinganData d) {
        if (d.stage <= 0) return "Sem Sharingan";
        if (d.stage <= 3) return "Sharingan " + d.stage + " Tomoe";
        if (d.stage == 4) return "Mangekyou Sharingan (" + TYPE_NAMES[d.type] + ")";
        return "Mangekyou Sharingan Eterno";
    }
}
