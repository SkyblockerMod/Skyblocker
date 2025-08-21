package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class DungeonBreakerFilter extends SimpleChatFilter {
	/* Messages:
	 * A mystical force prevents you from digging that block!
	 * A mystical force prevents you digging in this room!
	 * A mystical force prevents you digging there!
	 * You don't have enough charges to break this block right now!
	 */
	public DungeonBreakerFilter() {
		super("^(A mystical force prevents you (from digging that block|digging in this room|digging there)|You don't have enough charges to break this block right now)!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideDungeonBreaker;
	}
}
