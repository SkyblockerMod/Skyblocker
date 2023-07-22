package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;

public class AoteFilter extends SimpleChatFilter {
    public AoteFilter() {
        super("^There are blocks in the way!$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().messages.hideAOTE;
    }
}
