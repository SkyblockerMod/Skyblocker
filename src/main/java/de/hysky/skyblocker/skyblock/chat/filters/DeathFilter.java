package de.hysky.skyblocker.skyblock.chat.filters;

import java.util.regex.Matcher;

import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;

public class DeathFilter extends ChatPatternListener {

	public DeathFilter() {
		super(" \\u2620 .*");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideDeath;
	}

	@Override
	protected boolean onMatch(Component message, Matcher matcher) {
		return true;
	}
}
