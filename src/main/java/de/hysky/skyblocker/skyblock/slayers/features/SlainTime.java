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

			long totalSeconds = timeElapsed.getSeconds();
			long milliseconds = timeElapsed.toMillis() % 1000;

			String duration = String.format("%d.%02ds", totalSeconds, milliseconds / 10);

			Text message = Text.of(Constants.PREFIX.get().append("Slayer Boss has been killed in " + duration));
			if (PersonalBest.isPersonalBest(SlayerManager.getSlayerType() + SlayerManager.getSlayerTier(), timeElapsed)) {
				message = Text.of(Constants.PREFIX.get().append("Slayer Boss has been killed in " + duration).append(Text.literal(" PERSONAL BEST").formatted(Formatting.LIGHT_PURPLE)));
				PersonalBest.updateBestTime(SlayerManager.getSlayerType() + SlayerManager.getSlayerTier(), timeElapsed);
			}

			MinecraftClient.getInstance().player.sendMessage(message, false);
		}
	}
}
