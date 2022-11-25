package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

public class HealFilter extends SimpleChatFilter {
    public HealFilter() {
        super("^(?:You healed yourself for " + NUMBER + " health!|[a-zA-Z0-9_]{2,16} healed you for " + NUMBER + " health!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerMod.getInstance().CONFIG.messages.hideHeal();
    }
}
