package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.ContainerAndInventorySolver;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Set;

public class SellableItemsHighlighter extends SimpleContainerSolver implements ContainerAndInventorySolver {
	private static final Set<String> ITEM_IDS = Set.of(
			"DEFUSE_KIT",
			"TRAINING_WEIGHTS",
			"DUNGEON_LORE_PAPER",
			"REVIVE_STONE"
	);

	private static final Set<String> POTION_ITEM_NAMES = Set.of(
            "Healing VIII Splash Potion" // todo: check if need to also add without roman numeral
    );

	public SellableItemsHighlighter() {
		super("^(Ophelia|Booster Cookie)$");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return slots.int2ObjectEntrySet().stream()
				.filter(entry -> entry.getIntKey() > 53 && entry.getIntKey() < 81) // Only inventory slots
				.filter(entry -> isValidItem(entry.getValue())) // Match Skyblock Id
				.map(entry -> ColorHighlight.yellow(entry.getIntKey()))
				.toList();
	}

	private boolean isValidItem(ItemStack stack) {
		String skyblockId = stack.getSkyblockId();
		if (skyblockId.equals("POTION")) {
			String displayName = stack.getName().getString();
			return POTION_ITEM_NAMES.stream().anyMatch(displayName::contains);
		}
		return ITEM_IDS.stream().anyMatch(skyblockId::equals);
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().dungeons.sellableItemsHighlighter;
	}
}
