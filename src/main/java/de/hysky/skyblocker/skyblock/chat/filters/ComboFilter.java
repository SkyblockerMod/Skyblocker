package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.SkyBlockIcons;

public class ComboFilter extends SimpleChatFilter {
	public ComboFilter() {
		// ^(\+\d+ Kill Combo( \+\d+(%  Magic Find| coins per kill|☯ Combat Wisdom))?|Your Kill Combo has expired! You reached a \d+ Kill Combo!)$
		super(String.format("^(\\+\\d+ Kill Combo( \\+\\d+(%% %s Magic Find| coins per kill|☯ Combat Wisdom))?|Your Kill Combo has expired! You reached a \\d+ Kill Combo!)$", SkyBlockIcons.MAGIC_FIND));
	}

	@Override
	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideCombo;
	}
}
