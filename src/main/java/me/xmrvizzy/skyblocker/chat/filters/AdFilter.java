package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdFilter extends ChatPatternListener {
	private static final Pattern[] AD_FILTERS = new Pattern[] {
			Pattern.compile("^(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?(?:\\W|$)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(.)\\1{7,}"),
			Pattern.compile("\\W(?:on|in|check|at) my (?:ah|bin)(?:\\W|$)", Pattern.CASE_INSENSITIVE), };
	private static final String EMBLEMS = "\u2E15\u273F\u2741\u2E19\u03B1\u270E\u2615\u2616\u2663\u213B\u2694\u27B6\u26A1\u2604\u269A\u2693\u2620\u269B\u2666\u2660\u2764\u2727\u238A\u1360\u262C\u269D\u29C9\uA214\u32D6\u2E0E\u26A0\uA541\u3020\u30C4\u2948\u2622\u2623\u273E\u269C\u0BD0\u0A6D\u2742\u16C3\u3023\u10F6\u0444\u266A\u266B\u04C3\u26C1\u26C3\u16DD\uA03E\u1C6A\u03A3\u09EB\u2603\u2654\u12DE";

	public AdFilter() {
		// Groups:
		// 1. Player name
		// 2. Message
		// (?:§8\[[§feadbc0-9]+§8\] )?(?:[§76l]+[<INSERT EMBLEMS>] )?§[67abc](?:\[[§A-Za-z0-9+]+\] )?([A-Za-z0-9_]+)§[f7]: (.+)
		super("(?:§8\\[[§feadbc0-9]+§8\\] )?(?:[§76l]+[" + EMBLEMS + "] )?§[67abc](?:\\[[§A-Za-z0-9+]+\\] )?([A-Za-z0-9_]+)§[f7]: (.+)");
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