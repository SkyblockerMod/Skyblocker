package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class TeleportPadFilter extends ChatFilter {
    public TeleportPadFilter() {
        super("^(Warped from the .* Teleport Pad to the .* Teleport Pad!" +
                "|This Teleport Pad does not have a destination set!)$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideTeleportPad;
    }
}