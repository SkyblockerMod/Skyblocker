package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class ComboFilter extends SimpleChatFilter {
    public ComboFilter() {
        super("^(\\+\\d+ Kill Combo \\+\\d+(% ✯ Magic Find| coins per kill)" +
                "|Your Kill Combo has expired! You reached a \\d+ Kill Combo!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().messages.hideCombo;
    }
}
