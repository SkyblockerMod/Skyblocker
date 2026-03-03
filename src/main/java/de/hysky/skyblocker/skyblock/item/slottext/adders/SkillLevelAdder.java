package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.Utils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class SkillLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"skill_level",
			"skyblocker.config.uiAndVisuals.slotText.skillLevel");
	public SkillLevelAdder() {
		super("^Your Skills", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId / 9 < 1 || slotId / 9 > 4) return List.of();
		if (stack.getItem() == Items.BLACK_STAINED_GLASS_PANE) return List.of();
		String name = stack.getHoverName().getString();
		if (name.equals("Dungeoneering")) return List.of(); //This is a button to open the dungeon skill menu
		int lastIndex = name.lastIndexOf(' ');
		if (lastIndex == -1) return SlotText.bottomLeftList(Component.literal("0").withStyle(ChatFormatting.LIGHT_PURPLE));
		String romanNumeral = name.substring(lastIndex + 1); //+1 because we don't need the space itself
		//The "romanNumeral" might be a latin numeral, too. There's a skyblock setting for this, so we have to do it this way V
		if (ItemUtils.getLoreLineIf(stack, s -> s.contains("Max Skill level reached!")) != null) {
			return SlotText.bottomLeftList(Component.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Utils.parseInt(romanNumeral).orElse(0))).withColor(SlotText.GOLD));
		} else {
			return SlotText.bottomLeftList(Component.literal(String.valueOf(RomanNumerals.isValidRomanNumeral(romanNumeral) ? RomanNumerals.romanToDecimal(romanNumeral) : Utils.parseInt(romanNumeral).orElse(0))).withColor(SlotText.CREAM));
		}
	}
}
