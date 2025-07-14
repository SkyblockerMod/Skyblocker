package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class ToggleLotteryFilter extends SimpleChatFilter {

	public ToggleLotteryFilter() {
		super("^You can disable this messaging by toggling Lottery in your /hotf!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideToggleLottery;
	}
}
