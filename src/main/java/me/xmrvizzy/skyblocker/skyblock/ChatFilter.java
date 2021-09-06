package me.xmrvizzy.skyblocker.skyblock;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;


public class ChatFilter {
    public boolean shouldFilter(String message) {
        SkyblockerConfig.Messages settings = SkyblockerConfig.get().messages;
        if (settings.hideAbility && (message.contains("This ability is currently on cooldown for ") ||
                message.contains("No more charges, next one in ") ||
                message.contains("This ability is on cooldown for ")))
            return true;

        if (settings.hideHeal && (message.contains("You healed ") &&
                message.contains(" health!") || message.contains(" healed you for ")))
            return true;

        if (settings.hideAOTE && message.contains("There are blocks in the way!"))
            return true;

        if (settings.hideImplosion && message.contains("Your Implosion hit "))
            return true;

        if (settings.hideMoltenWave && message.contains("Your Molten Wave hit "))
            return true;
        return false;
    }
}
