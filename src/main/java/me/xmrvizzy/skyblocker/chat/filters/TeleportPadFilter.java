package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

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