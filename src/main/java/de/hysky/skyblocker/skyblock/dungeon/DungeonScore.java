package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonScore {
    private static final SkyblockerConfig.DungeonScore CONFIG = SkyblockerConfigManager.get().locations.dungeons.dungeonScore;
    private static final Pattern DUNGEON_CLEARED_PATTERN = Pattern.compile("Cleared: (?<cleared>\\d+)% \\((?<score>\\d+)\\)");
    private static boolean sent270;
    private static boolean sent300;

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(DungeonScore::tick, 20);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    }

    public static void tick() {
        if (!Utils.isInDungeons()) {
            reset();
            return;
        }

        for (String sidebarLine : Utils.STRING_SCOREBOARD) {
            Matcher dungeonClearedMatcher = DUNGEON_CLEARED_PATTERN.matcher(sidebarLine);
            if (!dungeonClearedMatcher.matches()) {
                continue;
            }
            int score = Integer.parseInt(dungeonClearedMatcher.group("score"));
            if (!DungeonManager.isInBoss()) score += 28;
            if (CONFIG.enableDungeonScore270 && !sent270 && score >= 270 && score < 300) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270"));
                sent270 = true;
            }
            if (CONFIG.enableDungeonScore300 && !sent300 && score >= 300) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300"));
                sent300 = true;
            }
            break;
        }
    }

    private static void reset() {
        sent270 = false;
        sent300 = false;
    }
}
