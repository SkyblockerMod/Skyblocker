package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdFilter extends ChatPatternListener {
	private static final Pattern[] AD_FILTERS = new Pattern[] {
			Pattern.compile("^(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?(?:\\W|$)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(.)\\1{7,}"),
			Pattern.compile("\\W(?:on|in|check|at) my (?:ah|bin)(?:\\W|$)", Pattern.CASE_INSENSITIVE), };

	public AdFilter() {
		// Groups:
		// 1. Player name
		// 2. Message
		// (?:\[[0-9]+\] )?(?:[<INSERT EMBLEMS>] )?(?:\[[A-Z+]+\] )?([A-Za-z0-9_]+): (.+)
		super("(?:\\[[0-9]+\\] )?(?:[" + Constants.LEVEL_EMBLEMS+ "] )?(?:\\[[A-Z+]+\\] )?([A-Za-z0-9_]+): (.+)");
	}

	@Override
	public boolean onMatch(Text _message, Matcher matcher) {
		String message = matcher.group(2);
		for (Pattern adFilter : AD_FILTERS)
			if (adFilter.matcher(message).find())
				return true;
		return false;
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().messages.hideAds;
	}
}