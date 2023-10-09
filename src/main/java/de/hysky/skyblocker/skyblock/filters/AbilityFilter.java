package de.hysky.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;

public class AbilityFilter extends SimpleChatFilter {
    public AbilityFilter() {
        super("^(?:This ability is on cooldown for " + NUMBER + "s\\.|No more charges, next one in " + NUMBER + "s!)$");
    }

    @Override
    protected ChatFilterResult state() {
        return SkyblockerConfigManager.get().messages.hideAbility;
    }
}
