package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class AoteFilter extends SimpleChatFilter {
    public AoteFilter() {
        super("^There are blocks in the way!$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().messages.hideAOTE;
    }
}
