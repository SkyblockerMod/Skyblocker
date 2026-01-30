package de.hysky.skyblocker.skyblock.slayers.features;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardsHighlighter extends SimpleContainerSolver {
	public RewardsHighlighter() {
		super(".*Slayer LVL Rewards$");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().slayers.highlightUnclaimedRewards;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack stack = entry.getValue();
			if (stack.has(DataComponents.LORE)) {
				List<String> lastLine = stack.skyblocker$getLoreStrings();
				if (!lastLine.isEmpty() && lastLine.getLast().equals("Click to claim rewards!")) {
					highlights.add(ColorHighlight.green(entry.getIntKey()));
				}
			}
		}
		return highlights;
	}
}
