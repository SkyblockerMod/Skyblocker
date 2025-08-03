package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class SkyMallFilter extends SimpleChatFilter {
	private static final String SKY_MALL_BUFF_CHANGED = "^New day! Your Sky Mall buff changed!$";
	//^New buff: Gain (?:.+) (?:Mining Speed|Mining Fortune|more Powder while mining|Pickaxe Ability cooldowns|chance to find Golden and Diamond Goblins|Titanium drops)\.$
	private static final String SKY_MALL_BUFF = "^New buff: Gain (?:.+) (?:Mining Speed|Mining Fortune|more Powder while mining|Pickaxe Ability cooldowns|chance to find Golden and Diamond Goblins|Titanium drops)\\.$";
	private static final String TOGGLE_SKY_MALL = "^You can disable this messaging by toggling Sky Mall in your /hotm!$";

	public SkyMallFilter() {
		super(SKY_MALL_BUFF_CHANGED + "|" + SKY_MALL_BUFF + "|" + TOGGLE_SKY_MALL);
	}

	@Override
	protected ChatFilterResult state() {
		return !Utils.isInDwarvenMines() || !Utils.isInCrystalHollows() ? SkyblockerConfigManager.get().chat.hideToggleSkyMall : ChatFilterResult.PASS;
	}
}
