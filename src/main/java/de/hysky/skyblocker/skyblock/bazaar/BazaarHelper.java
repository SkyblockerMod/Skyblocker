package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BazaarHelper extends ContainerSolver {
	private static final Pattern ORDER_PATTERN = Pattern.compile("You have [\\d,]+ (items|coins) to claim!");

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
		for (int slot = 0; slot < slots.size(); slot++) {
			if (ItemUtils.getLoreLineIf(slots.get(slot), str -> str.equals("Expired!")) != null) {
				highlights.add(ColorHighlight.red(slot));
				continue;
			}

			Matcher matcher = ItemUtils.getLoreLineIfMatch(slots.get(slot), ORDER_PATTERN);
			if (matcher != null) {
				switch (matcher.group(1)) {
					case "items" -> highlights.add(new ColorHighlight(slot, 0x7000AA00));
					case "coins" -> highlights.add(new ColorHighlight(slot, 0x70FFAA00));
				}
			}
		}
		return highlights;
	}
}
