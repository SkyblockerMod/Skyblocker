package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;

public class DeathFilter extends ChatPatternListener {

	public DeathFilter() {
		super(" \\u2620 .*");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideDeath;
	}

	@Override
	protected boolean onMatch(Text message, Matcher matcher) {
		return true;
	}
}
