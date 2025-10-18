package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class ShowOffFilter extends SimpleChatFilter {
	private static final String[] SHOW_TYPES = { "is holding", "is wearing", "is friends with a", "has" };

	public ShowOffFilter() {
		//(?:\[[0-9]+\] )?(?:[<INSERT EMBLEMS>] )?(?:\[[A-Z+]+\] )?([A-Za-z0-9_]+) (?:<INSERT SHOW TYPES>) \[(.+)\]
		super(Constants.PLAYER_NAME.pattern() + " (?:" + String.join("|", SHOW_TYPES) + ") \\[(.+)\\]");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideShowOff;
	}
}
