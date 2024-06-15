package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ColorTooltip extends TooltipAdder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ColorTooltip.class);

	public ColorTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.COLOR.isTooltipEnabledAndHasOrNullWarning(internalID) && stack.contains(DataComponentTypes.DYED_COLOR)) {
			//DyedColorComponent#getColor can be ARGB so we mask out the alpha bits
			int dyeColor = stack.get(DataComponentTypes.DYED_COLOR).rgb() & 0x00FFFFFF;
			String colorHex = String.format("%06X", dyeColor);
			String expectedHex = getExpectedHex(internalID);

			boolean correctLine = false;
			for (Text text : lines) {
				String existingTooltip = text.getString() + " ";
				if (existingTooltip.startsWith("Color: ")) {
					correctLine = true;

					addExoticTooltip(lines, internalID, ItemUtils.getCustomData(stack), colorHex, expectedHex, existingTooltip);
					break;
				}
			}

			if (!correctLine) {
				addExoticTooltip(lines, internalID, ItemUtils.getCustomData(stack), colorHex, expectedHex, "");
			}
		}
	}

	private static void addExoticTooltip(List<Text> lines, String internalID, NbtCompound customData, String colorHex, String expectedHex, String existingTooltip) {
		if (expectedHex != null && !colorHex.equalsIgnoreCase(expectedHex) && !isException(internalID, colorHex) && !intendedDyed(customData)) {
			final DyeType type = checkDyeType(colorHex);
			lines.add(1, Text.literal(existingTooltip + Formatting.DARK_GRAY + "(").append(type.getTranslatedText()).append(Formatting.DARK_GRAY + ")"));
		}
	}

	public static String getExpectedHex(String id) {
		String color = TooltipInfoType.COLOR.getData().get(id).getAsString();
		if (color != null) {
			String[] RGBValues = color.split(",");
			return String.format("%02X%02X%02X", Integer.parseInt(RGBValues[0]), Integer.parseInt(RGBValues[1]), Integer.parseInt(RGBValues[2]));
		} else {
			LOGGER.warn("[Skyblocker Exotics] No expected color data found for id {}", id);
			return null;
		}
	}

	public static boolean isException(String id, String hex) {
		return switch (id) {
			case String it when it.startsWith("LEATHER") || it.equals("GHOST_BOOTS") || Constants.SEYMOUR_IDS.contains(it) -> true;
			case String it when it.startsWith("RANCHER") -> Constants.RANCHERS.contains(hex);
			case String it when it.contains("ADAPTIVE_CHESTPLATE") -> Constants.ADAPTIVE_CHEST.contains(hex);
			case String it when it.contains("ADAPTIVE") -> Constants.ADAPTIVE.contains(hex);
			case String it when it.contains("REAPER") -> Constants.REAPER.contains(hex);
			case String it when it.contains("FAIRY") -> Constants.FAIRY_HEXES.contains(hex);
			case String it when it.contains("CRYSTAL") -> Constants.CRYSTAL_HEXES.contains(hex);
			case String it when it.contains("SPOOK") -> Constants.SPOOK.contains(hex);
			default -> false;
		};
	}

	public static DyeType checkDyeType(String hex) {
		return switch (hex) {
			case String it when Constants.CRYSTAL_HEXES.contains(it) -> DyeType.CRYSTAL;
			case String it when Constants.FAIRY_HEXES.contains(it) -> DyeType.FAIRY;
			case String it when Constants.OG_FAIRY_HEXES.contains(it) -> DyeType.OG_FAIRY;
			case String it when Constants.SPOOK.contains(it) -> DyeType.SPOOK;
			case String it when Constants.GLITCHED.contains(it) -> DyeType.GLITCHED;
			default -> DyeType.EXOTIC;
		};
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
