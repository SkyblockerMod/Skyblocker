package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HuntingBoxHelper extends SimpleContainerSolver {
	private static final Pattern OWNED_PATTERN = Pattern.compile("Owned: ([\\d,]+) Shards?");
	private static final Pattern SYPHON_PATTERN = Pattern.compile("Syphon (\\d+) more to level up!");
	private static final Logger LOGGER = LoggerFactory.getLogger(HuntingBoxHelper.class);

	public HuntingBoxHelper() {
		super("^Hunting Box$");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ArrayList<ColorHighlight> highlights = new ArrayList<>();
		ContainerSolver.trimEdges(slots, 6);
		for (var entry : slots.int2ObjectEntrySet()) {
			ItemStack stack = entry.getValue();
			if (stack.isEmpty()) continue;

			List<String> lore = stack.skyblocker$getLoreStrings();
			if (lore.isEmpty()) continue;

			String owned = null, syphon = null;
			for (String text : lore) { // We iterate manually instead of the ItemUtils helper methods because the lines are adjacent, this way we only iterate once rather than twice.
				if (owned == null) {
					Matcher matcher = OWNED_PATTERN.matcher(text);
					if (matcher.matches()) owned = matcher.group(1).replace(",", "");
				} else {
					Matcher matcher = SYPHON_PATTERN.matcher(text);
					if (matcher.matches()) syphon = matcher.group(1);
					break; // Somehow owned pattern matched but not syphon pattern
				}
			}
			if (owned == null || syphon == null) continue;
			int ownedCount = NumberUtils.toInt(owned, -1);
			int syphonCount = NumberUtils.toInt(syphon, -1);
			if (ownedCount < 0 || syphonCount < 0) {
				LOGGER.warn("Invalid owned or syphon count in Hunting Box: owned={}, syphon={}.", owned, syphon);
				continue;
			}
			if (ownedCount >= syphonCount) {
				boolean enoughButNotUnlocked = lore.getLast().startsWith("Requires");
				highlights.add(enoughButNotUnlocked ? ColorHighlight.yellow(entry.getIntKey())
													: ColorHighlight.green(entry.getIntKey()));
			}
		}
		return highlights;
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().hunting.huntingBox.enabled;
	}
}
