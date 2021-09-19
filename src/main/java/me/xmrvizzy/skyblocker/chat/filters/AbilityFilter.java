package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class AbilityFilter extends ChatListener {
    public AbilityFilter() {
        super("^(?:This ability is on cooldown for " + NUMBER + "s\\.|No more charges, next one in " + NUMBER + "s!)$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideAbility;
    }
}
