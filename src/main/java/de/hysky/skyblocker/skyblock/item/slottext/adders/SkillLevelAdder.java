package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.PositionedText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SkillLevelAdder extends SlotTextAdder {
	public SkillLevelAdder() {
		super("^Your Skills");
	}

	@Override
	public @NotNull List<PositionedText> getText(Slot slot) {
		switch (slot.id) {
			case 19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32 -> { //These are the slots that contain the skill items. Note that they aren't continuous, as there are 2 rows.
				String name = slot.getStack().getName().getString();
				int lastIndex = name.lastIndexOf(' ');
				if (lastIndex == -1) return List.of(PositionedText.BOTTOM_LEFT(Text.literal("0").formatted(Formatting.LIGHT_PURPLE))); //Skills without any levels don't display any roman numerals. Probably because 0 doesn't exist.
				String romanNumeral = name.substring(lastIndex + 1); //+1 because we don't need the space itself
				//The "romanNumeral" might be a latin numeral, too. There's a skyblock setting for this, so we have to do it this way V
				return List.of(PositionedText.BOTTOM_LEFT(Text.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Integer.parseInt(romanNumeral))).formatted(Formatting.LIGHT_PURPLE)));
			}
			default -> {
				return List.of();
			}
		}
	}
}
