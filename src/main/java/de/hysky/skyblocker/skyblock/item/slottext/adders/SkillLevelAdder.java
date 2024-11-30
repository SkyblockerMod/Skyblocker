package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkillLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"skill_level",
			"skyblocker.config.uiAndVisuals.slotText.rancherBoots");
	public SkillLevelAdder() {
		super("^Your Skills", CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		switch (slotId) {
			case 19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32 -> { //These are the slots that contain the skill items. Note that they aren't continuous, as there are 2 rows.
				String name = stack.getName().getString();
				int lastIndex = name.lastIndexOf(' ');
				if (lastIndex == -1) return SlotText.bottomLeftList(Text.literal("0").formatted(Formatting.LIGHT_PURPLE)); //Skills without any levels don't display any roman numerals. Probably because 0 doesn't exist.
				String romanNumeral = name.substring(lastIndex + 1); //+1 because we don't need the space itself
				//The "romanNumeral" might be a latin numeral, too. There's a skyblock setting for this, so we have to do it this way V
				if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Max Skill level reached!")) != null) {
					return SlotText.bottomLeftList(Text.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Integer.parseInt(romanNumeral))).withColor(0xE5B80B));
				} else {
					return SlotText.bottomLeftList(Text.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Integer.parseInt(romanNumeral))).withColor(0xFFDDC1));
				}
			}
			default -> {
				return List.of();
			}
		}
	}
}
