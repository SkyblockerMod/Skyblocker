package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinionLevelAdder extends SlotTextAdder {
	private static final Pattern MINION_PATTERN = Pattern.compile(".* Minion ([IVXLCDM]+)");
	public MinionLevelAdder() {
		super();
	}

	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.PLAYER_HEAD)) return List.of();
		Matcher matcher = MINION_PATTERN.matcher(itemStack.getName().getString());
		if (!matcher.matches()) return List.of();
		String romanNumeral = matcher.group(1);
		if (!RomanNumerals.isValidRomanNumeral(romanNumeral)) return List.of();
		int level = RomanNumerals.romanToDecimal(romanNumeral);
		return List.of(SlotText.topRight(Text.literal(String.valueOf(level)).formatted(Formatting.AQUA)));
	}
}
