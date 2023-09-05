package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;

public class TeleportPadFilter extends SimpleChatFilter {
    public TeleportPadFilter() {
        super("^(Warped from the .* Teleport Pad to the .* Teleport Pad!" +
                "|This Teleport Pad does not have a destination set!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().messages.hideTeleportPad;
    }
}