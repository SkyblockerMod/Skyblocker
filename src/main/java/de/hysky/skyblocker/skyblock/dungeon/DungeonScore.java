package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

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
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Utils.isInDungeons() || client.player == null) {
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
            if (!sent270 && score >= 270 && score < 300) {
                if (CONFIG.enableDungeonScore270Message) {
                    MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270"));
                }
                if (CONFIG.enableDungeonScore270Title) {
                    client.inGameHud.setDefaultTitleFade();
                    client.inGameHud.setTitle(Constants.PREFIX.get().append(CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270")));
                }
                if (CONFIG.enableDungeonScore270Sound) {
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f);
                }
                sent270 = true;
            }
            if (!sent300 && score >= 300) {
                if (CONFIG.enableDungeonScore300Message) {
                    MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300"));
                }
                if (CONFIG.enableDungeonScore300Title) {
                    client.inGameHud.setDefaultTitleFade();
                    client.inGameHud.setTitle(Constants.PREFIX.get().append(CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300")));
                }
                if (CONFIG.enableDungeonScore300Sound) {
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f);
                }
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
