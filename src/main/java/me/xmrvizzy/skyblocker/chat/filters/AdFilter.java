package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdFilter extends ChatPatternListener {
    private static final Pattern[] AD_FILTERS = new Pattern[]{
            Pattern.compile("^(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?(?:\\W|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(.)\\1{7,}"),
            Pattern.compile("\\W(?:on|in|check|at) my (?:ah|bin)(?:\\W|$)", Pattern.CASE_INSENSITIVE),
    };

    public AdFilter() {
        // Groups:
        // 1. Player name
        // 2. Message
        super("^§[67ab](?:\\[(?:MVP|VIP)(?:§[0-9a-f]\\+{1,2}§[6ab])?] )?([a-zA-Z0-9_]{2,16})§[7f]: (.*)$");
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
        return SkyblockerMod.getInstance().CONFIG.messages.hideAds();
    }
}