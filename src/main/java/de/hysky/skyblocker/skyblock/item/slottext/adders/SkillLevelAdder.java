package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.annotations.RegisterSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RegisterSlotTextAdder
public class SkillLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"skill_level",
			"skyblocker.config.uiAndVisuals.slotText.skillLevel");
	public SkillLevelAdder() {
		super("^Your Skills", CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slotId / 9 < 1 || slotId / 9 > 4) return List.of();
		if (stack.getItem() == Items.BLACK_STAINED_GLASS_PANE) return List.of();
		String name = stack.getName().getString();
		if (name.equals("Dungeoneering")) return List.of(); //This is a button to open the dungeon skill menu
		int lastIndex = name.lastIndexOf(' ');
		if (lastIndex == -1) return SlotText.bottomLeftList(Text.literal("0").formatted(Formatting.LIGHT_PURPLE));
		String romanNumeral = name.substring(lastIndex + 1); //+1 because we don't need the space itself
		//The "romanNumeral" might be a latin numeral, too. There's a skyblock setting for this, so we have to do it this way V
		if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Max Skill level reached!")) != null) {
			return SlotText.bottomLeftList(Text.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Utils.parseInt(romanNumeral).orElse(0))).withColor(SlotText.GOLD));
		} else {
			return SlotText.bottomLeftList(Text.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Utils.parseInt(romanNumeral).orElse(0))).withColor(SlotText.CREAM));
		}
	}
}
