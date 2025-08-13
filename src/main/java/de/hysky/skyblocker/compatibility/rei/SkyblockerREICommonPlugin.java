package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;

public class SkyblockerREICommonPlugin implements REICommonPlugin {
	@Override
	public void registerItemComparators(ItemComparatorRegistry registry) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		registry.registerGlobal(new SkyblockItemComparator());
	}
}
