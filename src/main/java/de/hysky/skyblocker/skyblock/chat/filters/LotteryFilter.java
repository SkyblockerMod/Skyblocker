package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class LotteryFilter extends SimpleChatFilter {
	private static final String LOTTERY_BUFF_CHANGED = "^New day! Your Lottery buff changed!$";
	//^New buff: Gain (?:.+) (?:Fig Fortune|Mangrove Fortune|Sweep)\.$
	private static final String LOTTERY_BUFF = "^New buff: Gain (?:.+) (?:Fig Fortune|Mangrove Fortune|Sweep)\\.$";
	private static final String TOGGLE_LOTTERY = "^You can disable this messaging by toggling Lottery in your /hotf!$";

	public LotteryFilter() {
		super(LOTTERY_BUFF_CHANGED + "|" + LOTTERY_BUFF + "|"+ TOGGLE_LOTTERY);
	}

	@Override
	protected ChatFilterResult state() {
		return !Utils.isInGalatea() ? SkyblockerConfigManager.get().chat.hideToggleLottery : ChatFilterResult.PASS;
	}
}
