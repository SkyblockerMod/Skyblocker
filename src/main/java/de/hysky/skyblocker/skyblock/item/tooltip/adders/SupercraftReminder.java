package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SupercraftReminder extends SimpleTooltipAdder {
	private static final byte SUPERCRAFT_SLOT = 32;
	private static final byte RECIPE_RESULT_SLOT = 25;

	public SupercraftReminder() {
		super(Pattern.compile("^.+ Recipe$"), Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (focusedSlot == null || focusedSlot.index != SUPERCRAFT_SLOT || !stack.is(Items.GOLDEN_PICKAXE)) return;
		String uuid = focusedSlot.container.getItem(RECIPE_RESULT_SLOT).getUuid();
		if (!uuid.isEmpty()) return; //Items with UUID can't be stacked, and therefore the shift-click feature doesn't matter
		int index = lines.size() - 1;
		if (lines.get(lines.size() - 2).getString().equals("Recipe not unlocked!")) index--; //Place it right below the "Right-Click to set amount" line
		lines.add(index, Component.literal("Shift-Click to maximize the amount!").withStyle(ChatFormatting.GOLD));
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
