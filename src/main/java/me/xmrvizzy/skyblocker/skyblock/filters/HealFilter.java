package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;

public class HealFilter extends SimpleChatFilter {
    public HealFilter() {
        super("^(?:You healed yourself for " + NUMBER + " health!|[a-zA-Z0-9_]{2,16} healed you for " + NUMBER + " health!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().messages.hideHeal;
    }
}
