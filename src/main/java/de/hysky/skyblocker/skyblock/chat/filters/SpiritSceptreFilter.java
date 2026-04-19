package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class SpiritSceptreFilter extends SimpleChatFilter {
	public SpiritSceptreFilter() {
		super("^Your Spirit Sceptre hit " + NUMBER + " enem(?:y|ies) for " + NUMBER + " damage\\.$");
	}

	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideSpiritSceptre;
	}
}
