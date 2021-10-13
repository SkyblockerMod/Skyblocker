package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class AoteFilter extends ChatFilter {
    public AoteFilter() {
        super("^There are blocks in the way!$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideAOTE;
    }
}