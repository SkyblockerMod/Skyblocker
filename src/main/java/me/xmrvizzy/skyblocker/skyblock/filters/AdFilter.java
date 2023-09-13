package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Constants;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.utils.chat.ChatPatternListener;
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
		// (?:§8\[[§feadbc0-9]+§8\] )?(?:[§76l]+[<INSERT EMBLEMS>] )?§[67abc](?:\[[§A-Za-z0-9+]+\] )?([A-Za-z0-9_]+)§[f7]: (.+)
		super("(?:§8\\[[§feadbc0-9]+§8\\] )?(?:[§76l]+[" + Constants.LEVEL_EMBLEMS + "] )?§[67abc](?:\\[[§A-Za-z0-9+]+\\] )?([A-Za-z0-9_]+)§[f7]: (.+)");
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
		return SkyblockerConfig.get().messages.hideAds;
	}
}