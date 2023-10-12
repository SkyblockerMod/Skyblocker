package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class ShowOffFilter extends SimpleChatFilter {
	private static final String[] SHOW_TYPES = { "is holding", "is wearing", "is friends with a", "has" };

	public ShowOffFilter() {
		super("(?:§8\\[[§feadbc0-9]+§8\\] )?(?:[§76l]+[" + Constants.LEVEL_EMBLEMS + "] )?§[67abc](?:\\[[§A-Za-z0-9+]+\\] )?([A-Za-z0-9_]+)[§f7]+ (?:" + String.join("|", SHOW_TYPES) + ") §8\\[(.+)§8\\]");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().messages.hideShowOff;
	}
}
