package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnchantmentLevelAdder extends SlotTextAdder {
	public EnchantmentLevelAdder() {
		super();
	}

	@Override
	public @Nullable Text getText(Slot slot) {
		final ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.ENCHANTED_BOOK)) return null;
		String name = itemStack.getName().getString();
		if (name.equals("Enchanted Book")) {
			List<Text> lore = ItemUtils.getLore(itemStack);
			if (lore.isEmpty()) return null;
			int level = getEnchantLevelFromString(lore.getFirst().getString());
			if (level == 0) return null;
			return Text.literal(String.valueOf(level)).formatted(Formatting.GREEN);
		} else { //In bazaar, the books have the enchantment name in the name
			int level = getEnchantLevelFromString(name);
			if (level == 0) return null;
			return Text.literal(String.valueOf(level)).formatted(Formatting.GREEN);
		}
	}

	private static int getEnchantLevelFromString(String str) {
		String romanNumeral = str.substring(str.lastIndexOf(' ') + 1); //+1 because we don't need the space itself
		return ChocolateFactorySolver.romanToDecimal(romanNumeral); //Temporary line. The method will be moved out later.
	}
}
