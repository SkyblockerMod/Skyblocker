package de.hysky.skyblocker.compatibility.rei;

import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;

public class SkyblockerREICommonPlugin implements REICommonPlugin {
	@Override
	public void registerItemComparators(ItemComparatorRegistry registry) {
		if (!Utils.isOnSkyblock()) return;
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		registry.registerGlobal(new SkyblockItemComparator());
	}
}
