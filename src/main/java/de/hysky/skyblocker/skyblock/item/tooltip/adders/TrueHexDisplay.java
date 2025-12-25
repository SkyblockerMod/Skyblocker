package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;

/**
 * Changes the color of HEX colors codes on dye items to reflect their actual color
 */
public class TrueHexDisplay extends SimpleTooltipAdder {
	private static final Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");

	public TrueHexDisplay() {
		super(Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		String itemId = stack.getSkyblockId();

		//Nice job on item id consistency Hypixel
		if (itemId != null && !itemId.isEmpty() && (itemId.startsWith("DYE_") || itemId.endsWith("_DYE"))) {
			for (Component line : lines) {
				//The hex part is inside of the siblings
				for (Component text : line.getSiblings()) {
					String stringified = text.getString();

					if (HEX_PATTERN.matcher(stringified).matches()) {
						((MutableComponent) text).withColor(Integer.decode(stringified));
					}
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
