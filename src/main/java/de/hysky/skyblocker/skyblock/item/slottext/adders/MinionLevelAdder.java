package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class MinionLevelAdder extends SimpleSlotTextAdder {
	private static final Pattern MINION_PATTERN = Pattern.compile(".* Minion ([IVXLCDM]+)");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"minion_level",
			"skyblocker.config.uiAndVisuals.slotText.minionLevel");
	public MinionLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.is(Items.PLAYER_HEAD)) return List.of();
		Matcher matcher = MINION_PATTERN.matcher(stack.getHoverName().getString());
		if (!matcher.matches()) return List.of();
		String romanNumeral = matcher.group(1);
		if (!RomanNumerals.isValidRomanNumeral(romanNumeral)) return List.of();
		int level = RomanNumerals.romanToDecimal(romanNumeral);
		return SlotText.topRightList(Component.literal(String.valueOf(level)).withColor(SlotText.CREAM));
	}
}
