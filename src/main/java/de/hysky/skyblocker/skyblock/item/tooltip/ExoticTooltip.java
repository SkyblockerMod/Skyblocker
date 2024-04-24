package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.utils.Constants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public class ExoticTooltip {
    public static String getExpectedHex(String id) {
        String color = TooltipInfoType.COLOR.getData().get(id).getAsString();
        if (color != null) {
            String[] RGBValues = color.split(",");
            return String.format("%02X%02X%02X", Integer.parseInt(RGBValues[0]), Integer.parseInt(RGBValues[1]), Integer.parseInt(RGBValues[2]));
        } else {
            ItemTooltip.LOGGER.warn("[Skyblocker Exotics] No expected color data found for id {}", id);
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

    public static DyeType checkDyeType(String hex) {
        if (Constants.CRYSTAL_HEXES.contains(hex)) {
            return DyeType.CRYSTAL;
        }
        if (Constants.FAIRY_HEXES.contains(hex)) {
            return DyeType.FAIRY;
        }
        if (Constants.OG_FAIRY_HEXES.contains(hex)) {
            return DyeType.OG_FAIRY;
        }
        if (Constants.SPOOK.contains(hex)) {
            return DyeType.SPOOK;
        }
        if (Constants.GLITCHED.contains(hex)) {
            return DyeType.GLITCHED;
        }
        return DyeType.EXOTIC;
    }

    public static boolean intendedDyed(NbtCompound customData) {
        return customData.contains("dye_item");
    }

    public enum DyeType implements StringIdentifiable {
        CRYSTAL("crystal", Formatting.AQUA),
        FAIRY("fairy", Formatting.LIGHT_PURPLE),
        OG_FAIRY("og_fairy", Formatting.DARK_PURPLE),
        SPOOK("spook", Formatting.RED),
        GLITCHED("glitched", Formatting.BLUE),
        EXOTIC("exotic", Formatting.GOLD);
        private final String name;
        private final Formatting formatting;

        DyeType(String name, Formatting formatting) {
            this.name = name;
            this.formatting = formatting;
        }

        @Override
        public String asString() {
            return name;
        }

        public MutableText getTranslatedText() {
            return Text.translatable("skyblocker.exotic." + name).formatted(formatting);
        }
    }
}
