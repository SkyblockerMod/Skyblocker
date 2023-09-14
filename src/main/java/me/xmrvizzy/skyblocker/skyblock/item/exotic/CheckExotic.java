package me.xmrvizzy.skyblocker.skyblock.item.exotic;

import com.google.gson.JsonObject;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CheckExotic {

    static String[] SeymourIDS = {"VELVET_TOP_HAT", "CASHMERE_JACKET", "SATIN_TROUSERS", "OXFORD_SHOES"};
    public static String getExpectedHex(String id) {
        JsonObject item = DownloadColors.ItemApiData.getAsJsonObject(id);
        if (item != null) {
            if (item.has("color")) {
                String[] RGBValues = item.get("color").getAsString().split(",");
                String hex = String.format("%02x%02x%02x", Integer.parseInt(RGBValues[0]), Integer.parseInt(RGBValues[1]), Integer.parseInt(RGBValues[2]));
                return hex.toUpperCase();
            } else {
                System.out.println("Color isn't part of NBT");
                return null;
            }
        } else {
            System.out.println("Item is null");
            return null;
        }
    }

    public static Boolean checkExceptions(String id, String hex) {
        if (id.startsWith("LEATHER") || id.equals("GHOST_BOOTS") || ListContainsString(SeymourIDS, id)) {
            return true;
        }
        if (id.startsWith("RANCHER")) {
            return ListContainsString(HexArrays.Ranchers, hex);
        }
        if (id.contains("ADAPTIVE_CHESTPLATE")) {
            return ListContainsString(HexArrays.AdaptiveChest, hex);
        } else if (id.contains("ADAPTIVE")) {
            return ListContainsString(HexArrays.Adaptive, hex);
        }
        if (id.startsWith("REAPER")) {
            return ListContainsString(HexArrays.Reaper, hex);
        }
        if (id.startsWith("FAIRY")) {
            return ListContainsString(HexArrays.FairyHexes, hex);
        }
        if (id.startsWith("CRYSTAL")) {
            return ListContainsString(HexArrays.CrystalHexes, hex);
        }
        if (id.contains("SPOOK")) {
            return ListContainsString(HexArrays.Spook, hex);
        }
        return false;
    }

    public static String checkDyeType(String ActualHex) {
        if (ListContainsString(HexArrays.CrystalHexes, ActualHex)) {
            return "CRYSTAL";
        }
        if (ListContainsString(HexArrays.FairyHexes, ActualHex)) {
            return "FAIRY";
        }
        if (ListContainsString(HexArrays.OgFairyHexes, ActualHex)) {
            return "OG_FAIRY";
        }
        if (ListContainsString(HexArrays.Spook, ActualHex)) {
            return "SPOOK";
        }
        if (ListContainsString(HexArrays.Glitched, ActualHex)) {
            return "GLITCHED";
        }
        return "EXOTIC";
    }

    private static Boolean ListContainsString(String[] list, String s) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equalsIgnoreCase(s)) {
                return  true;
            }
        }
        return false;
    }

    public static Formatting FormattingColor(String s) {
        switch (s) {
            case "CRYSTAL": return Formatting.AQUA;
            case "FAIRY": return Formatting.LIGHT_PURPLE;
            case "OG_FAIRY": return Formatting.DARK_PURPLE;
            case "SPOOK": return Formatting.RED;
            case "GLITCHED": return Formatting.BLUE;
            case "EXOTIC": return Formatting.GOLD;
        }
        return Formatting.DARK_GRAY;
    }

    public static Text getTranslatatedText(String s) {
        switch (s) {
            case "CRYSTAL": return Text.translatable("skyblocker.exotic.crystal");
            case "FAIRY": return Text.translatable("skyblocker.exotic.fairy");
            case "OG_FAIRY": return Text.translatable("skyblocker.exotic.og_fairy");
            case "SPOOK": return Text.translatable("skyblocker.exotic.spook");
            case "GLITCHED": return Text.translatable("skyblocker.exotic.glitched");
            case "EXOTIC": return Text.translatable("skyblocker.exotic.exotic");
        }
        return null;
    }
}
