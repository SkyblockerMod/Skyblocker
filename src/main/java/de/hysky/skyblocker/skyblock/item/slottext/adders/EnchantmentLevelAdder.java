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
		List<Text> lore = ItemUtils.getLore(itemStack);
		if (lore.isEmpty()) return null;
		String firstLine = lore.getFirst().getString();
		String romanNumeral = firstLine.substring(firstLine.lastIndexOf(' ') + 1); //+1 because we don't need the space itself
		int level = ChocolateFactorySolver.romanToDecimal(romanNumeral); //Temporary line. The method will be moved out later.
		if (level == 0) return null;
		return Text.literal(String.valueOf(level)).formatted(Formatting.BLUE);
	}
}
