package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class ShowOffFilter extends SimpleChatFilter {
	private static final String EMBLEMS = "\u2E15\u273F\u2741\u2E19\u03B1\u270E\u2615\u2616\u2663\u213B\u2694\u27B6\u26A1\u2604\u269A\u2693\u2620\u269B\u2666\u2660\u2764\u2727\u238A\u1360\u262C\u269D\u29C9\uA214\u32D6\u2E0E\u26A0\uA541\u3020\u30C4\u2948\u2622\u2623\u273E\u269C\u0BD0\u0A6D\u2742\u16C3\u3023\u10F6\u0444\u266A\u266B\u04C3\u26C1\u26C3\u16DD\uA03E\u1C6A\u03A3\u09EB\u2603\u2654\u12DE";
	private static final String[] SHOW_TYPES = { "is holding", "is wearing", "is friends with a", "has" };

	public ShowOffFilter() {
		super("(?:§8\\[[§feadbc0-9]+§8\\] )?(?:[§76l]+[" + EMBLEMS + "] )?§[67abc](?:\\[[§A-Za-z0-9+]+\\] )?([A-Za-z0-9_]+)[§f7]+ (?:" + String.join("|", SHOW_TYPES) + ") §8\\[(.+)§8\\]");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfig.get().messages.hideShowOff;
	}
}
