package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class LineSmoothener extends SimpleTooltipAdder {
	//This is static to not create a new text object for each line in every item
	private static final Component BUMPY_LINE = Component.literal("-----------------").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH);

	public static Component createSmoothLine() {
		return Component.literal("                    ").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH, ChatFormatting.BOLD);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public LineSmoothener() {
		super(Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		for (int i = 0; i < lines.size(); i++) {
			List<Component> lineSiblings = lines.get(i).getSiblings();
			//Compare the first sibling rather than the whole object as the style of the root object can change while visually staying the same
			if (lineSiblings.size() == 1 && lineSiblings.getFirst().equals(BUMPY_LINE)) {
				lines.set(i, createSmoothLine());
			}
		}
	}
}
