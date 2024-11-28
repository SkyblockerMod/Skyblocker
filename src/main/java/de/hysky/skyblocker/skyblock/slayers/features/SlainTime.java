package de.hysky.skyblocker.skyblock.slayers.features;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Duration;
import java.time.Instant;

public class SlainTime {

	public static void onBossDeath(Instant startTime) {
		if (SkyblockerConfigManager.get().slayers.slainTime & startTime != null) {
			Instant slainTime = Instant.now();
			Duration timeElapsed = Duration.between(startTime, slainTime);
			String duration = formatTime(timeElapsed);

			Duration currentPB = PersonalBest.getPersonalBest(SlayerManager.getSlayerType() + SlayerManager.getSlayerTier());

			if (currentPB != null && (currentPB.toMillis() > timeElapsed.toMillis())) {
				MinecraftClient.getInstance().player.sendMessage(Text.of(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.slainTime", Text.literal(duration).formatted(Formatting.YELLOW))).append(Text.translatable("skyblocker.slayer.personalBest").formatted(Formatting.LIGHT_PURPLE))), false);
				MinecraftClient.getInstance().player.sendMessage(Text.of(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.previousPB", Text.literal(formatTime(currentPB)).formatted(Formatting.YELLOW)))), false);
				PersonalBest.updateBestTime(SlayerManager.getSlayerType() + SlayerManager.getSlayerTier(), timeElapsed);
			} else {
				MinecraftClient.getInstance().player.sendMessage(Text.of(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.slainTime", Text.literal(duration).formatted(Formatting.YELLOW)))), false);
				if (currentPB == null) {
					PersonalBest.updateBestTime(SlayerManager.getSlayerType() + SlayerManager.getSlayerTier(), timeElapsed);
				}
			}
		}
	}

	private static String formatTime(Duration timeElapsed) {
		long totalSeconds = timeElapsed.getSeconds();
		long milliseconds = timeElapsed.toMillis() % 1000;

		return String.format("%d.%02ds", totalSeconds, milliseconds / 10);
	}
}
