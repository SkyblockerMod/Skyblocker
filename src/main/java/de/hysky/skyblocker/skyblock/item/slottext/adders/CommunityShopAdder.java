package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommunityShopAdder extends SlotTextAdder {
	private static final byte CATEGORIES_START = 10;
	private static final byte CATEGORIES_END = 14; //Inclusive
	private static byte currentScreen = -1; // 0 = city projects, 1 = upgrades, 2 = booster cookie, 3 = bits shop, 4 = fire sales, any other number = invalid

	public CommunityShopAdder() {
		super("^Community Shop");
	}

	@Override
	public @NotNull List<SlotText> getText(@NotNull ItemStack itemStack, int slotId) {
		if (slotId >= CATEGORIES_START && slotId <= CATEGORIES_END && itemStack.isOf(Items.LIME_STAINED_GLASS_PANE)) { //Only the selected category has a lime stained glass pane, the others have a gray one.
			currentScreen = (byte) (slotId - CATEGORIES_START);
			return List.of();
		}
		return switch (currentScreen) { //This is a switch statement to allow easily adding other categories in the future.
			case 1 -> getTextForUpgradesScreen(itemStack, slotId);
			default -> List.of();
		};
	}

	private static List<SlotText> getTextForUpgradesScreen(ItemStack stack, int slotId) {
		switch (slotId) {
			case 30, 31, 32, 33, 34, 38, 39, 40, 41, 42, 43, 44 -> {
				String name = stack.getName().getString();
				int lastIndex = name.lastIndexOf(' ');
				String roman = name.substring(lastIndex + 1); // + 1 as we don't want the space
				if (!RomanNumerals.isValidRomanNumeral(roman)) return List.of();

				List<Text> lore = ItemUtils.getLore(stack);
				if (lore.isEmpty()) return List.of();
				String lastLine = lore.getLast().getString();
				return List.of(SlotText.bottomLeft(switch (lastLine) {
					case "Maxed out!" -> Text.literal("Max").withColor(0xfab387);
					case "Currently upgrading!", "Click to instantly upgrade!" -> Text.literal("⏰").withColor(0xf9e2af).formatted(Formatting.BOLD);
					case "Click to claim!" -> Text.literal("✅").withColor(0xa6e3a1).formatted(Formatting.BOLD);
					default -> Text.literal(String.valueOf(RomanNumerals.romanToDecimal(roman))).withColor(0xcba6f7);
				}));
			}
			default -> {
				return List.of();
			}
		}
	}
}
