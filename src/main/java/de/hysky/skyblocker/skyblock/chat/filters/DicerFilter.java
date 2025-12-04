package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class DicerFilter extends SimpleChatFilter {
	public DicerFilter() {
		super("[A-Z]+ DROP! .*Dicer dropped [0-9]+x.+!$");
	}

	@Override
	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideDicer;
	}
}
