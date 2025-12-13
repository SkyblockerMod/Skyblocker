package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * Changes the color of HEX colors codes on dye items to reflect their actual color
 */
public class TrueHexDisplay extends SimpleTooltipAdder {
	private static final Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");

	public TrueHexDisplay() {
		super(Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		String itemId = stack.getSkyblockId();

		//Nice job on item id consistency Hypixel
		if (itemId != null && !itemId.isEmpty() && (itemId.startsWith("DYE_") || itemId.endsWith("_DYE"))) {
			for (Text line : lines) {
				//The hex part is inside of the siblings
				for (Text text : line.getSiblings()) {
					String stringified = text.getString();

					if (HEX_PATTERN.matcher(stringified).matches()) {
						((MutableText) text).withColor(Integer.decode(stringified));
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
