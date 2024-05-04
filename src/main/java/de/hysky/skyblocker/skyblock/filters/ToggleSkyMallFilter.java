package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class ToggleSkyMallFilter extends SimpleChatFilter {

	public ToggleSkyMallFilter() {
		super("^You can disable this messaging by toggling Sky Mall in your /hotm!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().messages.hideToggleSkyMall;
	}
}
