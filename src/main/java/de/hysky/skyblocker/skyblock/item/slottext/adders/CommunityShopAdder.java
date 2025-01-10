package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slotId >= CATEGORIES_START && slotId <= CATEGORIES_END && stack.isOf(Items.LIME_STAINED_GLASS_PANE)) { //Only the selected category has a lime stained glass pane, the others have a gray one.
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
				String name = stack.getName().getString();
				int lastIndex = name.lastIndexOf(' ');
				String roman = name.substring(lastIndex + 1); // + 1 as we don't want the space
				if (!RomanNumerals.isValidRomanNumeral(roman)) yield List.of();

				List<Text> lore = ItemUtils.getLore(stack);
				if (lore.isEmpty()) yield List.of();
				String lastLine = lore.getLast().getString();
				yield SlotText.bottomLeftList(switch (lastLine) {
					case "Maxed out!" -> Text.literal("Max").withColor(0xfab387);
					case "Currently upgrading!", "Click to instantly upgrade!" -> Text.literal("⏰").withColor(0xf9e2af).formatted(Formatting.BOLD);
					case "Click to claim!" -> Text.literal("✅").withColor(0xa6e3a1).formatted(Formatting.BOLD);
					default -> Text.literal(String.valueOf(RomanNumerals.romanToDecimal(roman))).withColor(0xcba6f7);
				});
			}
			default -> List.of();
		};
	}
}
