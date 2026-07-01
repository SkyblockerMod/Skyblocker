package de.hysky.skyblocker.skyblock;

import java.util.List;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RaffleRewardHighlight extends SimpleContainerSolver {
	public RaffleRewardHighlight() {
		super("Previous Raffles");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.centuryRaffle.enableRaffleRewardHighlight;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ObjectArrayList<ColorHighlight> highlights = new ObjectArrayList<>();

		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack itemStack = entry.getValue();

			// Skip items that aren't valid
			if (!itemStack.is(Items.FIREWORK_ROCKET) || !itemStack.is(Items.DIAMOND_BLOCK) || !itemStack.is(Items.GOLD_BLOCK)) {
				continue;
			}

			List<String> lore = itemStack.skyblocker$getLoreStrings();

			if (lore.isEmpty()) {
				continue;
			}

			switch (lore.getLast()) {
				case "You claimed your prize!" -> highlights.add(ColorHighlight.green(entry.getIntKey()));
				case "Click to reveal your prize!" -> highlights.add(ColorHighlight.red(entry.getIntKey()));
			}
		}

		return highlights;
	}
}
