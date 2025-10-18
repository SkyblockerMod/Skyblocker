package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class AoteFilter extends SimpleChatFilter {
    public AoteFilter() {
        super("^There are blocks in the way!$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().chat.hideAOTE;
    }
}
