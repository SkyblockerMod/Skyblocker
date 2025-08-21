package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class DungeonBreakerFilter extends SimpleChatFilter {
	/* Messages:
	 * A mystical force prevents you from digging that block!
	 * You don't have enough charges to break this block right now!
	 * A mystical force prevents you digging in this room!
	 */
	public DungeonBreakerFilter() {
		super("^(A mystical force prevents you from digging that block!|You don't have enough charges to break this block right now!|A mystical force prevents you digging in this room!)$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideDungeonBreaker;
	}
}
