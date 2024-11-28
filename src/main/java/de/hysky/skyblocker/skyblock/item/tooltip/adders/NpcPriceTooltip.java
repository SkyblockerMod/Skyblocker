package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NpcPriceTooltip extends SimpleTooltipAdder {
	public NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.NPC.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		// NPC prices seem to use the Skyblock item id, not the Skyblock api id.
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.NPC.hasOrNullWarning(internalID)) {
			int amount;
			if (lines.get(1).getString().endsWith("Sack")) {
				//The amount is in the 2nd sibling of the 3rd line of the lore.                                              here V
				//Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
				String line = lines.get(3).getSiblings().get(1).getString().replace(",", "");
				amount = NumberUtils.isParsable(line) && !line.equals("0") ? Integer.parseInt(line) : stack.getCount();
			} else {
				amount = stack.getCount();
			}
			lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
			              .formatted(Formatting.YELLOW)
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.NPC.getData().getDouble(internalID), amount)));
		}
	}
}
