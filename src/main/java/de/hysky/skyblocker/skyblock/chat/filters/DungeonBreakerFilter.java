package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class DungeonBreakerFilter extends SimpleChatFilter {
	public DungeonBreakerFilter() {
		super("^(A mystical force prevents you (from digging that block|digging in this room|digging there)|You don't have enough charges to break this block right now)!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideDungeonBreaker;
	}
}
