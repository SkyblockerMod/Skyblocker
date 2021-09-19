package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class HealFilter extends ChatListener {
    public HealFilter() {
        super("^(?:You healed yourself for " + NUMBER + " health!|[a-zA-Z0-9_]{2,16} healed you for " + NUMBER + " health!)$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideHeal;
    }
}
