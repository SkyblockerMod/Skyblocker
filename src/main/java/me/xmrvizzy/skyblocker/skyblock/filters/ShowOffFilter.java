package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.utils.Constants;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class ShowOffFilter extends SimpleChatFilter {
	private static final String[] SHOW_TYPES = { "is holding", "is wearing", "is friends with a", "has" };

	public ShowOffFilter() {
		super("(?:§8\\[[§feadbc0-9]+§8\\] )?(?:[§76l]+[" + Constants.LEVEL_EMBLEMS + "] )?§[67abc](?:\\[[§A-Za-z0-9+]+\\] )?([A-Za-z0-9_]+)[§f7]+ (?:" + String.join("|", SHOW_TYPES) + ") §8\\[(.+)§8\\]");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfig.get().messages.hideShowOff;
	}
}
