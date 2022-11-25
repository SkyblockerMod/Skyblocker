package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

public class TeleportPadFilter extends SimpleChatFilter {
    public TeleportPadFilter() {
        super("^(Warped from the .* Teleport Pad to the .* Teleport Pad!" +
                "|This Teleport Pad does not have a destination set!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerMod.getInstance().CONFIG.messages.hideTeleportPad();
    }
}