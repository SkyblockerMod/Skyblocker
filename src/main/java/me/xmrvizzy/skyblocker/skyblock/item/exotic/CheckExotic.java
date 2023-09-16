package me.xmrvizzy.skyblocker.skyblock.item.exotic;

import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.utils.Constants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CheckExotic {
    public static String getExpectedHex(String id) {
        String color = PriceInfoTooltip.colorJson.get(id).getAsString();
        if (color != null) {
            String[] RGBValues = color.split(",");
            String hex = String.format("%02x%02x%02x", Integer.parseInt(RGBValues[0]), Integer.parseInt(RGBValues[1]), Integer.parseInt(RGBValues[2]));
            return hex.toUpperCase();
        } else {
            System.out.println("Color is null");
            return null;
        }
    }

    public static boolean isException(String id, String hex) {
        if (id.startsWith("LEATHER") || id.equals("GHOST_BOOTS") || Constants.SEYMOUR_IDS.contains(id)) {
            return true;
        }
        if (id.startsWith("RANCHER")) {
            return Constants.RANCHERS.contains(hex);
        }
        if (id.contains("ADAPTIVE_CHESTPLATE")) {
            return Constants.ADAPTIVE_CHEST.contains(hex);
        } else if (id.contains("ADAPTIVE")) {
            return Constants.ADAPTIVE.contains(hex);
        }
        if (id.startsWith("REAPER")) {
            return Constants.REAPER.contains(hex);
        }
        if (id.startsWith("FAIRY")) {
            return Constants.FAIRY_HEXES.contains(hex);
        }
        if (id.startsWith("CRYSTAL")) {
            return Constants.CRYSTAL_HEXES.contains(hex);
        }
        if (id.contains("SPOOK")) {
            return Constants.SPOOK.contains(hex);
        }
        return false;
    }

    public static String checkDyeType(String hex) {
        if (Constants.CRYSTAL_HEXES.contains(hex)) {
            return "CRYSTAL";
        }
        if (Constants.FAIRY_HEXES.contains(hex)) {
            return "FAIRY";
        }
        if (Constants.OG_FAIRY_HEXES.contains(hex)) {
            return "OG_FAIRY";
        }
        if (Constants.SPOOK.contains(hex)) {
            return "SPOOK";
        }
        if (Constants.GLITCHED.contains(hex)) {
            return "GLITCHED";
        }
        return "EXOTIC";
    }

    public static boolean intendedDyed(NbtCompound ItemData) {
        return ItemData.getCompound("ExtraAttributes").contains("dye_item");
    }

    public static Formatting getFormattingColor(String s) {
        return switch (s) {
            case "CRYSTAL" -> Formatting.AQUA;
            case "FAIRY" -> Formatting.LIGHT_PURPLE;
            case "OG_FAIRY" -> Formatting.DARK_PURPLE;
            case "SPOOK" -> Formatting.RED;
            case "GLITCHED" -> Formatting.BLUE;
            case "EXOTIC" -> Formatting.GOLD;
            default -> Formatting.DARK_GRAY;
        };
    }

    public static MutableText getTranslatedText(String s) {
        return Text.translatable("skyblocker.exotic." + s.toLowerCase());
    }
}
