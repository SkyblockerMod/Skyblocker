package de.hysky.skyblocker.skyblock.item.tooltip.info;

import java.util.function.Predicate;

import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;

public class SimpleTooltipInfo implements TooltipInfoType {
	private final Predicate<GeneralConfig.ItemTooltip> tooltipEnabled;

	protected SimpleTooltipInfo(Predicate<GeneralConfig.ItemTooltip> tooltipEnabled) {
		this.tooltipEnabled = tooltipEnabled;
	}

	@Override
	public boolean isTooltipEnabled() {
		return tooltipEnabled.test(ItemTooltip.config);
	}
}
