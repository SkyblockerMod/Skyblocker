package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class MinionLevelAdder extends SlotTextAdder {
	public MinionLevelAdder() {
		super();
	}

	@Override
	public @Nullable Text getText(Slot slot) {
		ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.PLAYER_HEAD)) return null;
		String name = itemStack.getName().getString();
		if (!name.contains("Minion")) return null;
		String romanNumeral = name.substring(name.lastIndexOf(' ') + 1); //+1 because we don't need the space itself
		int level = RomanNumerals.romanToDecimal(romanNumeral);
		if (level == 0) return null;
		return Text.literal(String.valueOf(level)).formatted(Formatting.DARK_PURPLE);
	}
}
