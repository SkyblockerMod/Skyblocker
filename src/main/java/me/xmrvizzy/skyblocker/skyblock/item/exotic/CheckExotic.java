package me.xmrvizzy.skyblocker.skyblock.item.exotic;

import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.utils.Constants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CheckExotic {
    static String[] SeymourIDS = {"VELVET_TOP_HAT", "CASHMERE_JACKET", "SATIN_TROUSERS", "OXFORD_SHOES"};
    public static String getExpectedHex(String id) {
        String color = PriceInfoTooltip.ColorApiData.get(id).getAsString();
        if (color != null) {
            String[] RGBValues = color.split(",");
            String hex = String.format("%02x%02x%02x", Integer.parseInt(RGBValues[0]), Integer.parseInt(RGBValues[1]), Integer.parseInt(RGBValues[2]));
            return hex.toUpperCase();
        } else {
            System.out.println("Color is null");
            return null;
        }
    }

    public static Boolean checkExceptions(String id, String hex) {
        if (id.startsWith("LEATHER") || id.equals("GHOST_BOOTS") || ListContainsString(SeymourIDS, id)) {
            return true;
        }
        if (id.startsWith("RANCHER")) {
            return ListContainsString(Constants.Ranchers, hex);
        }
        if (id.contains("ADAPTIVE_CHESTPLATE")) {
            return ListContainsString(Constants.AdaptiveChest, hex);
        } else if (id.contains("ADAPTIVE")) {
            return ListContainsString(Constants.Adaptive, hex);
        }
        if (id.startsWith("REAPER")) {
            return ListContainsString(Constants.Reaper, hex);
        }
        if (id.startsWith("FAIRY")) {
            return ListContainsString(Constants.FairyHexes, hex);
        }
        if (id.startsWith("CRYSTAL")) {
            return ListContainsString(Constants.CrystalHexes, hex);
        }
        if (id.contains("SPOOK")) {
            return ListContainsString(Constants.Spook, hex);
        }
        return false;
    }

    public static String checkDyeType(String ActualHex) {
        if (ListContainsString(Constants.CrystalHexes, ActualHex)) {
            return "CRYSTAL";
        }
        if (ListContainsString(Constants.FairyHexes, ActualHex)) {
            return "FAIRY";
        }
        if (ListContainsString(Constants.OgFairyHexes, ActualHex)) {
            return "OG_FAIRY";
        }
        if (ListContainsString(Constants.Spook, ActualHex)) {
            return "SPOOK";
        }
        if (ListContainsString(Constants.Glitched, ActualHex)) {
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

    public static Boolean intendedDyed(NbtCompound ItemData) {
        return ItemData.getCompound("ExtraAttributes").getKeys().contains("dye_item");
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
