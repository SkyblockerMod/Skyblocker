package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ColorTooltip extends SimpleTooltipAdder {
	private static final long WITHER_GLITCHED_AFTER_DATE = 1605830400000L;

	public ColorTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.COLOR.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.COLOR.hasOrNullWarning(internalID) && stack.has(DataComponents.DYED_COLOR)) {
			//DyedColorComponent#getColor can be ARGB so we mask out the alpha bits
			int dyeColor = stack.get(DataComponents.DYED_COLOR).rgb() & 0x00FFFFFF;
			String colorHex = String.format("%06X", dyeColor);
			String expectedHex = getExpectedHex(internalID);

			boolean correctLine = false;
			for (Component text : lines) {
				String existingTooltip = text.getString() + " ";
				if (existingTooltip.startsWith("Color: ")) {
					correctLine = true;

					addExoticTooltip(lines, stack, internalID, colorHex, expectedHex, existingTooltip);
					break;
				}
			}

			if (!correctLine) {
				addExoticTooltip(lines, stack, internalID, colorHex, expectedHex, "");
			}
		}
	}

	private static void addExoticTooltip(List<Component> lines, ItemStack stack, String internalID, String colorHex, String expectedHex, String existingTooltip) {
		if (expectedHex != null && !colorHex.equalsIgnoreCase(expectedHex) && !isException(internalID, colorHex) && !intendedDyed(ItemUtils.getCustomData(stack))) {
			final DyeType type = checkDyeType(stack, colorHex);
			lines.add(1, Component.literal(existingTooltip + ChatFormatting.DARK_GRAY + "(")
					.append(type.getTranslatedText())
					.append(Component.literal(" - "))
					.append(Component.literal("#" + colorHex).withColor(Integer.decode("0x" + colorHex)))
					.append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)));
		}
	}

	private static String getExpectedHex(String id) {
		String color = TooltipInfoType.COLOR.getData().get(id);
		if (color != null) {
			String[] RGBValues = color.split(",");
			return String.format("%02X%02X%02X", Integer.parseInt(RGBValues[0]), Integer.parseInt(RGBValues[1]), Integer.parseInt(RGBValues[2]));
		} else {
			return null;
		}
	}

	private static boolean isException(String id, String hex) {
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

	private static DyeType checkDyeType(ItemStack stack, String hex) {
		return switch (hex) {
			case String it when Constants.CRYSTAL_HEXES.contains(it) -> DyeType.CRYSTAL;
			case String it when Constants.FAIRY_HEXES.contains(it) -> DyeType.FAIRY;
			case String it when Constants.OG_FAIRY_HEXES.contains(it) -> DyeType.OG_FAIRY;
			case String it when Constants.SPOOK.contains(it) && stack.getSkyblockId().startsWith("FAIRY_") -> DyeType.SPOOK;
			case String it when Constants.GLITCHED.contains(it) && isGlitched(stack, hex) -> DyeType.GLITCHED;
			default -> DyeType.EXOTIC;
		};
	}

	private static boolean intendedDyed(CompoundTag customData) {
		return customData.contains("dye_item");
	}

	//Thanks to TGWaffles' guidance on how to make this more accurate
	private static boolean isGlitched(ItemStack stack, String hex) {
		String id = stack.getSkyblockId();

		if (id.contains("WITHER")) {
			return isWitherGlitched(id, hex, ObtainedDateTooltip.getLongTimestamp(stack));
		}

		String miscGlitchedId = Constants.MISC_GLITCHED_HEXES.get(hex);

		return miscGlitchedId != null && id.startsWith(miscGlitchedId);
	}

	private static boolean isWitherGlitched(String id, String hex, long obtained) {
		if (hex.equals("000000") && obtained < WITHER_GLITCHED_AFTER_DATE) return false; //Too old to be glitched

		return switch (id) {
			case String it when it.contains("CHESTPLATE") -> Constants.WITHER_CHESTPLATE_HEXES.containsKey(hex) && Constants.WITHER_CHESTPLATE_HEXES.containsValue(it) && !Constants.WITHER_CHESTPLATE_HEXES.get(hex).equals(it);
			case String it when it.contains("LEGGINGS") -> Constants.WITHER_LEGGINGS_HEXES.containsKey(hex) && Constants.WITHER_LEGGINGS_HEXES.containsValue(it) && !Constants.WITHER_LEGGINGS_HEXES.get(hex).equals(it);
			case String it when it.contains("BOOTS") -> Constants.WITHER_BOOTS_HEXES.containsKey(hex) && Constants.WITHER_BOOTS_HEXES.containsValue(it) && !Constants.WITHER_BOOTS_HEXES.get(hex).equals(it);

			default -> false;
		};
	}

	private enum DyeType implements StringRepresentable {
		CRYSTAL("crystal", ChatFormatting.AQUA),
		FAIRY("fairy", ChatFormatting.LIGHT_PURPLE),
		OG_FAIRY("og_fairy", ChatFormatting.DARK_PURPLE),
		SPOOK("spook", ChatFormatting.RED),
		GLITCHED("glitched", ChatFormatting.BLUE),
		EXOTIC("exotic", ChatFormatting.GOLD);

		private final String name;
		private final ChatFormatting formatting;

		DyeType(String name, ChatFormatting formatting) {
			this.name = name;
			this.formatting = formatting;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public MutableComponent getTranslatedText() {
			return Component.translatable("skyblocker.exotic." + name).withStyle(formatting);
		}
	}

}
