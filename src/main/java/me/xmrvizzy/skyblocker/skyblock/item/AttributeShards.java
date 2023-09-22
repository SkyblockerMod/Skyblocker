package me.xmrvizzy.skyblocker.skyblock.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AttributeShards {
    private static final Object2ObjectOpenHashMap<String, String> ID_2_SHORT_NAME = new Object2ObjectOpenHashMap<>();

    static {
        //Weapons
        ID_2_SHORT_NAME.put("arachno", "A");
        ID_2_SHORT_NAME.put("attack_speed", "AS");
        ID_2_SHORT_NAME.put("blazing", "BL");
        ID_2_SHORT_NAME.put("combo", "C");
        ID_2_SHORT_NAME.put("elite", "E");
        ID_2_SHORT_NAME.put("ender", "EN");
        ID_2_SHORT_NAME.put("ignition", "I");
        ID_2_SHORT_NAME.put("life_recovery", "LR");
        ID_2_SHORT_NAME.put("mana_steal", "MS");
        ID_2_SHORT_NAME.put("midas_touch", "MT");
        ID_2_SHORT_NAME.put("undead", "U");

        //Swords & Bows
        ID_2_SHORT_NAME.put("warrior", "W");
        ID_2_SHORT_NAME.put("deadeye", "DE");

        //Armor or Equipment
        ID_2_SHORT_NAME.put("arachno_resistance", "AR");
        ID_2_SHORT_NAME.put("blazing_resistance", "BR");
        ID_2_SHORT_NAME.put("breeze", "B");
        ID_2_SHORT_NAME.put("dominance", "D");
        ID_2_SHORT_NAME.put("ender_resistance", "ER");
        ID_2_SHORT_NAME.put("experience", "XP");
        ID_2_SHORT_NAME.put("fortitude", "F");
        ID_2_SHORT_NAME.put("life_regeneration", "HR"); //Health regeneration
        ID_2_SHORT_NAME.put("lifeline", "L");
        ID_2_SHORT_NAME.put("magic_find", "MF");
        ID_2_SHORT_NAME.put("mana_pool", "MP");
        ID_2_SHORT_NAME.put("mana_regeneration", "MR");
        ID_2_SHORT_NAME.put("mending", "V"); //Vitality
        ID_2_SHORT_NAME.put("speed", "S");
        ID_2_SHORT_NAME.put("undead_resistance", "UR");
        ID_2_SHORT_NAME.put("veteran", "V");

        //Fishing Gear
        ID_2_SHORT_NAME.put("blazing_fortune", "BF");
        ID_2_SHORT_NAME.put("fishing_experience", "FE");
        ID_2_SHORT_NAME.put("infection", "IF");
        ID_2_SHORT_NAME.put("double_hook", "DH");
        ID_2_SHORT_NAME.put("fisherman", "FM");
        ID_2_SHORT_NAME.put("fishing_speed", "FS");
        ID_2_SHORT_NAME.put("hunter", "H");
        ID_2_SHORT_NAME.put("trophy_hunter", "TH");

    }

    public static String getShortName(String id) {
        return ID_2_SHORT_NAME.getOrDefault(id, "");
    }
}
