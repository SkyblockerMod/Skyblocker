package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

public class AbilityFilter extends SimpleChatFilter {
    public AbilityFilter() {
        super("^(?:This ability is on cooldown for " + NUMBER + "s\\.|No more charges, next one in " + NUMBER + "s!)$");
    }

    @Override
    protected ChatFilterResult state() {
        return SkyblockerMod.getInstance().CONFIG.messages.hideAbility();
    }
}
