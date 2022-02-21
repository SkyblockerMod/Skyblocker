package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class TeleportPadFilter extends ChatFilter {
    public TeleportPadFilter() {
        super("^.*Teleport Pad (does not have a destination set!|to the).*$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideTeleportPad;
    }
}