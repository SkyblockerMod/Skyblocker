package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class SkillLevelAdder extends SlotTextAdder {
	public SkillLevelAdder() {
		super("^Your Skills");
	}

	@Override
	public @Nullable Text getText(Slot slot) {
		switch (slot.id) {
			case 19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32 -> { //These are the slots that contain the skill items. Note that they aren't continuous, as there are 2 rows.
				String name = slot.getStack().getName().getString();
				int lastIndex = name.lastIndexOf(' ');
				if (lastIndex == -1) return Text.literal("0").formatted(Formatting.LIGHT_PURPLE); //Skills without any levels don't display any roman numerals. Probably because 0 doesn't exist.
				String romanNumeral = name.substring(lastIndex + 1); //+1 because we don't need the space itself
				if (!RomanNumerals.isValidRomanNumeral(romanNumeral)) return null;
				return Text.literal(String.valueOf(RomanNumerals.romanToDecimal(romanNumeral))).formatted(Formatting.LIGHT_PURPLE);
			}
			default -> {
				return null;
			}
		}
	}
}
