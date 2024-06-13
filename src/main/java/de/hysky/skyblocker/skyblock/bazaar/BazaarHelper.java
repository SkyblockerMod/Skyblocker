package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BazaarHelper extends ContainerSolver {
	private static final Pattern ORDER_PATTERN = Pattern.compile("You have [\\d,]+ (items|coins) to claim!");
	private static final Pattern FILLED_PATTERN = Pattern.compile("Filled: \\S+ \\(?([\\d.]+)%\\)?!?");

	public BazaarHelper() {
		super("Your Bazaar Orders");
	}

	@Override
	protected boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableBazaarHelper;
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		ArrayList<ColorHighlight> highlights = new ArrayList<>();
		// Skip the first and last 10 slots as those are always glass panes.
		for (int slot = 10; slot < slots.size() - 10; slot++) {
			ItemStack item = slots.get(slot);
			if (item.isEmpty() || item.isOf(Items.BLACK_STAINED_GLASS_PANE)) continue;
			if (ItemUtils.getLoreLineIf(slots.get(slot), str -> str.equals("Expired!")) != null) {
				highlights.add(ColorHighlight.red(slot));
				continue;
			}
			switch (SkyblockerConfigManager.get().helpers.bazaar.highlightingScheme) {
				case ORDER_TYPE -> {
					Matcher matcher = ItemUtils.getLoreLineIfMatch(slots.get(slot), ORDER_PATTERN);
					if (matcher != null) {
						switch (matcher.group(1)) {
							case "items" -> highlights.add(new ColorHighlight(slot, Formatting.DARK_GREEN.getColorValue() & 0x70000000));
							case "coins" -> highlights.add(new ColorHighlight(slot, Formatting.GOLD.getColorValue() & 0x70000000));
						}
					}
				}
				case FULFILLMENT -> {
					Matcher matcher = ItemUtils.getLoreLineIfMatch(slots.get(slot), FILLED_PATTERN);
					if (matcher != null) {
						int filled = NumberUtils.toInt(matcher.group(1));
						if (filled < 100) {
							highlights.add(ColorHighlight.yellow(slot));
						} else if (filled == 100) {
							highlights.add(ColorHighlight.green(slot));
						}
					}
				}
			}
		}

		return highlights;
	}

	public enum HighlightingScheme {
		ORDER_TYPE,
		FULFILLMENT;

		@Override
		public String toString() {
			return switch (this) {
				case ORDER_TYPE -> "Order Type";
				case FULFILLMENT -> "Fulfillment";
			};
		}
	}
}
