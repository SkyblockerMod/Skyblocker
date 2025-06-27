package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//TODO: Add required amount of shards to reach max level in the tooltip via a TooltipAdder implementation once people get enough shards to figure them out
//      It can be seen by clicking on the attribute but that's too much effort, we could bring that up a layer, into the tooltip of the attribute
public class AttributeLevelHelper extends SimpleSlotTextAdder {
	public static final AttributeLevelHelper INSTANCE = new AttributeLevelHelper();
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation("attribute_levels",
			"skyblocker.config.uiAndVisuals.slotText.attributeLevels",
			"skyblocker.config.uiAndVisuals.slotText.attributeLevels.@Tooltip");


	private AttributeLevelHelper() {
		super("Attribute Menu", CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slot == null || stack.isEmpty()) return List.of();
		if (slot.id <= 9 || slot.id >= 44) return List.of(); // Don't need to process the first row and the last row
		if (stack.isOf(Items.BLACK_STAINED_GLASS_PANE)) return List.of();

		Text customName = stack.getCustomName();
		if (customName == null) return List.of();

		String itemName = customName.getString();
		String levelText = StringUtils.substringAfterLast(itemName, " ");
		if (levelText.isEmpty() || !RomanNumerals.isValidRomanNumeral(levelText)) return List.of();

		int level = RomanNumerals.romanToDecimal(levelText);
		if (level < 1 || level > 10) return List.of(); // Should not go beyond these bounds, but just in case.

		return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(level == 10 ? SlotText.GOLD : SlotText.CREAM));
	}
}
