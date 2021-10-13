package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

import java.util.regex.Pattern;

public class AdFilter extends ChatFilter {
    private static final String regex;

    public AdFilter() {
        super(regex);
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideAds;
    }

    static {
        StringBuilder sb = new StringBuilder("^§[67ab](?:\\[(?:MVP|VIP)(?:§[0-9a-f]\\+{1,2}§[6ab])?] )?([a-zA-Z0-9_]{2,16})§[7f]: ");
        String[] inexact = new String[] {
                "(?:on|in|check|at) my ah",
                "(?>(.)\\2{7,})",
        };
        String[] exact = new String[]{
                "(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?(?:\\W|).*",
        };
        sb.append("(?i:.*(?:");
        sb.append(inexact[0]);
        for(int i = 1; i < inexact.length; i++) {
            sb.append("|");
            sb.append(inexact[i]);
        }
        sb.append(").*");
        for (String s : exact) {
            sb.append("|");
            sb.append(s);
        }
        sb.append(")$");
        regex = sb.toString();
    }
}