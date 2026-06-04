package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
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
		if (!itemId.isEmpty() && (itemId.startsWith("DYE_") || itemId.endsWith("_DYE"))) {
			for (int i = 0; i < lines.size(); i++) {
				Component line = lines.get(i).copy();

				//The hex part is inside the siblings
				List<Component> siblings = line.getSiblings();
				for (int j = 0; j < siblings.size(); j++) {
					Component text = siblings.get(j);
					String stringified = text.getString();

					if (HEX_PATTERN.matcher(stringified).matches()) {
						text = text.copy().withColor(Integer.decode(stringified));
						siblings.set(j, text);
						lines.set(i, line);
					}
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return !SkyblockerConfigManager.get().debug.enableRepoDev;
	}
}
