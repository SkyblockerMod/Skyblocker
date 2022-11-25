package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

public class AoteFilter extends SimpleChatFilter {
    public AoteFilter() {
        super("^There are blocks in the way!$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerMod.getInstance().CONFIG.messages.hideAOTE();
    }
}
