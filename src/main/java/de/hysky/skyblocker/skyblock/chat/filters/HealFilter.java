package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class HealFilter extends SimpleChatFilter {
    public HealFilter() {
        super("^(?:You healed yourself for " + NUMBER + " health!|[a-zA-Z0-9_]{2,16} healed you for " + NUMBER + " health!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().chat.hideHeal;
    }
}
