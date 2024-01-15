package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;

public class MimicFilter extends ChatPatternListener {
    public MimicFilter() {
        super("(?:Mimic dead!?|Mimic Killed!|\\$SKYTILS-DUNGEON-SCORE-MIMIC\\$|\\Q" + SkyblockerConfigManager.get().locations.dungeons.mimicMessages.mimicMessage + "\\E)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().messages.hideMimicKill;
    }

    @Override
    protected boolean onMatch(Text message, Matcher matcher) {
        if (!Utils.isInDungeons()) return false;
        DungeonScore.setMimicKilled(true);
        return true;
    }
}
