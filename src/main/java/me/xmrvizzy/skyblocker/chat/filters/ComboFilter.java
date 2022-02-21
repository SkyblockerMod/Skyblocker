package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class ComboFilter extends ChatFilter {
    public ComboFilter() {
        super("^.*Kill Combo (\\+|has expired!).*$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.hideCombo;
    }
}