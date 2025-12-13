package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class CommunityShopAdder extends SimpleSlotTextAdder {
	private static final byte CATEGORIES_START = 10;
	private static final byte CATEGORIES_END = 14; //Inclusive
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"community_shop",
			"skyblocker.config.uiAndVisuals.slotText.communityShop",
			"skyblocker.config.uiAndVisuals.slotText.communityShop.@Tooltip");
	private static byte currentScreen = -1; // 0 = city projects, 1 = upgrades, 2 = booster cookie, 3 = bits shop, 4 = fire sales, any other number = invalid

	public CommunityShopAdder() {
		super("^Community Shop", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId >= CATEGORIES_START && slotId <= CATEGORIES_END && stack.is(Items.LIME_STAINED_GLASS_PANE)) { //Only the selected category has a lime stained glass pane, the others have a gray one.
			currentScreen = (byte) (slotId - CATEGORIES_START);
			return List.of();
		}
		return switch (currentScreen) { //This is a switch statement to allow easily adding other categories in the future.
			case 1 -> getTextForUpgradesScreen(stack, slotId);
			default -> List.of();
		};
	}

	private static List<SlotText> getTextForUpgradesScreen(ItemStack stack, int slotId) {
		return switch (slotId) {
			case 30, 31, 32, 33, 34, 38, 39, 40, 41, 42, 43, 44 -> {
				String name = stack.getHoverName().getString();
				int lastIndex = name.lastIndexOf(' ');
				String roman = name.substring(lastIndex + 1); // + 1 as we don't want the space
				if (!RomanNumerals.isValidRomanNumeral(roman)) yield List.of();

				List<String> lore = stack.skyblocker$getLoreStrings();
				if (lore.isEmpty()) yield List.of();
				String lastLine = lore.getLast();
				yield SlotText.bottomLeftList(switch (lastLine) {
					case "Maxed out!" -> Component.literal("Max").withColor(SlotText.LIGHT_ORANGE);
					case "Currently upgrading!", "Click to instantly upgrade!" -> Component.literal("⏰").withColor(SlotText.LIGHT_YELLOW).withStyle(ChatFormatting.BOLD);
					case "Click to claim!" -> Component.literal("✅").withColor(0xA6E3A1).withStyle(ChatFormatting.BOLD);
					default -> Component.literal(String.valueOf(RomanNumerals.romanToDecimal(roman))).withColor(SlotText.LIGHT_PURPLE);
				});
			}
			default -> List.of();
		};
	}
}
