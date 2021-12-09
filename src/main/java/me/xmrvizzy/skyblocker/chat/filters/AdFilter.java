package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

import java.util.regex.Pattern;

public class AdFilter extends ChatFilter {
    private static final Pattern[] AD_FILTERS = new Pattern[]{
            Pattern.compile("^(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?(?:\\W|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(.)\\1{7,}"),
            Pattern.compile("\\W(?:on|in|check|at) my (?:ah|bin)(?:\\W|$)", Pattern.CASE_INSENSITIVE),
    };
    public AdFilter() {
        super("^§[67ab](?:\\[(?:MVP|VIP)(?:§[0-9a-f]\\+{1,2}§[6ab])?] )?([a-zA-Z0-9_]{2,16})§[7f]: (.*)$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideAds;
    }

    @Override
    public boolean onMessage(String[] groups) {
        for(Pattern adFilter : AD_FILTERS)
            if(adFilter.matcher(groups[2]).find())
                return true;
        return false;
    }
}