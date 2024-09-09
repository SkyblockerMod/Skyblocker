package de.hysky.skyblocker.skyblock.slayers.features;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.time.Duration;
import java.time.Instant;

public class SlainTime {

    public static void onBossDeath(Instant startTime) {
        if (SkyblockerConfigManager.get().slayers.slainTime) {
            if (startTime == null) {
                System.out.println("[Skyblocker] Slayer quest has not started yet.");
                return;
            }

            Instant slainTime = Instant.now();
            Duration timeElapsed = Duration.between(startTime, slainTime);

            long totalSeconds = timeElapsed.getSeconds();
            long milliseconds = timeElapsed.toMillis() % 1000;

            String duration = String.format("%d.%02ds", totalSeconds, milliseconds / 10);
            MinecraftClient.getInstance().player.sendMessage(Text.of(Constants.PREFIX.get().append("Slayer Boss has been killed in " + duration)));
        }
    }
}
