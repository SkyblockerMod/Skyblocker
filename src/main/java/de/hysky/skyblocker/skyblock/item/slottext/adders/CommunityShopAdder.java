package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommunityShopAdder extends SlotTextAdder {
	private static final byte CATEGORIES_START = 10;
	private static final byte CATEGORIES_END = 14; //Inclusive

	public CommunityShopAdder() {
		super("^Community Shop");
	}

	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		for (byte i = CATEGORIES_START; i <= CATEGORIES_END; i++) {
			if (slot.inventory.getStack(i).isOf(Items.LIME_STAINED_GLASS_PANE)) { //Only the selected category has a lime stained glass pane, the others have a gray one.
				return switch (i) { //This is a switch to allow adding more categories easily in the future, if we ever add more.
					case 11 -> getTextForUpgradesScreen(slot);
					default -> List.of();
				};
			}
		}
		return List.of();
	}

	private static List<SlotText> getTextForUpgradesScreen(Slot slot) {
		final ItemStack stack = slot.getStack();
		switch (slot.id) {
			case 30, 31, 32, 33, 34,  38, 39, 40, 41, 42, 43, 44 -> {
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
		}
		return List.of();
	}
}
