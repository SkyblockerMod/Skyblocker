package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Pattern;

public class SupercraftReminder extends TooltipAdder {
	public SupercraftReminder() {
		super(Pattern.compile("^.+ Recipe$"), Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(List<Text> lines, Slot focusedSlot) {
		if (focusedSlot.id != 32 || !focusedSlot.getStack().isOf(Items.GOLDEN_PICKAXE)) return;
		lines.add(lines.size()-1, Text.literal("Shift-Click to maximize the amount!").formatted(Formatting.GOLD));
	}
}
