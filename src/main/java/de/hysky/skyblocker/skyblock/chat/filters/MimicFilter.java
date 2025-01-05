package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class MimicFilter extends SimpleChatFilter {
    public MimicFilter() {
        super(".*?(?:Mimic dead!?|Mimic Killed!|\\$SKYTILS-DUNGEON-SCORE-MIMIC\\$|\\Q" + SkyblockerConfigManager.get().dungeons.mimicMessage.mimicMessage + "\\E)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().chat.hideMimicKill;
    }
}
