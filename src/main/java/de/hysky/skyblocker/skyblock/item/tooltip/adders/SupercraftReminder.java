package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

public class SupercraftReminder extends SimpleTooltipAdder {
	private static final byte SUPERCRAFT_SLOT = 32;
	private static final byte RECIPE_RESULT_SLOT = 25;

	public SupercraftReminder() {
		super(Pattern.compile("^.+ Recipe$"), Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (focusedSlot == null || focusedSlot.id != SUPERCRAFT_SLOT || !stack.isOf(Items.GOLDEN_PICKAXE)) return;
		String uuid = focusedSlot.inventory.getStack(RECIPE_RESULT_SLOT).getUuid();
		if (!uuid.isEmpty()) return; //Items with UUID can't be stacked, and therefore the shift-click feature doesn't matter
		int index = lines.size() - 1;
		if (lines.get(lines.size() - 2).getString().equals("Recipe not unlocked!")) index--; //Place it right below the "Right-Click to set amount" line
		lines.add(index, Text.literal("Shift-Click to maximize the amount!").formatted(Formatting.GOLD));
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
