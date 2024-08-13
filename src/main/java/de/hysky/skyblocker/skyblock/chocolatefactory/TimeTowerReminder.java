package de.hysky.skyblocker.skyblock.chocolatefactory;

import com.mojang.brigadier.Message;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TimeTowerReminder {
	private static final String TIME_TOWER_FILE = "time_tower.txt";
	private static final Pattern TIME_TOWER_PATTERN = Pattern.compile("^TIME TOWER! Your Chocolate Factory production has increased by \\+[\\d.]+x for \\dh!$");
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Time Tower Reminder");
	private static boolean scheduled = false;

	private TimeTowerReminder() {
	}

	@Init
	public static void init() {
		SkyblockEvents.JOIN.register(TimeTowerReminder::checkTempFile);
		ClientReceiveMessageEvents.GAME.register(TimeTowerReminder::checkIfTimeTower);
	}

	public static void checkIfTimeTower(Message message, boolean overlay) {
		if (!TIME_TOWER_PATTERN.matcher(message.getString()).matches() || scheduled) return;
		Scheduler.INSTANCE.schedule(TimeTowerReminder::sendMessage, 60 * 60 * 20); // 1 hour
		scheduled = true;
		File tempFile = SkyblockerMod.CONFIG_DIR.resolve(TIME_TOWER_FILE).toFile();
		if (!tempFile.exists()) {
			try {
				tempFile.createNewFile();
			} catch (IOException e) {
				LOGGER.error("[Skyblocker Time Tower Reminder] Failed to create temp file for Time Tower Reminder!", e);
				return;
			}
		}

		try (FileWriter writer = new FileWriter(tempFile)) {
			writer.write(String.valueOf(System.currentTimeMillis())); //Overwrites the file so no need to handle case where the file already exists and has text
		} catch (IOException e) {
			LOGGER.error("[Skyblocker Time Tower Reminder] Failed to write to temp file for Time Tower Reminder!", e);
		}
	}

	private static void sendMessage() {
		if (MinecraftClient.getInstance().player == null || !Utils.isOnSkyblock()) return;
		if (SkyblockerConfigManager.get().helpers.chocolateFactory.enableTimeTowerReminder) {
			MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.literal("Your Chocolate Factory's Time Tower has deactivated!").formatted(Formatting.RED)));
		}
		File tempFile = SkyblockerMod.CONFIG_DIR.resolve(TIME_TOWER_FILE).toFile();
		try {
			scheduled = false;
			if (tempFile.exists()) Files.delete(tempFile.toPath());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Time Tower Reminder] Failed to delete temp file for Time Tower Reminder!", e);
		}
	}

	private static void checkTempFile() {
		File tempFile = SkyblockerMod.CONFIG_DIR.resolve(TIME_TOWER_FILE).toFile();
		if (!tempFile.exists() || scheduled) return;

		long time;
		try (Stream<String> file = Files.lines(tempFile.toPath())) {
			time = Long.parseLong(file.findFirst().orElseThrow());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Time Tower Reminder] Failed to read temp file for Time Tower Reminder!", e);
			return;
		}

		if (System.currentTimeMillis() - time >= 60 * 60 * 1000) sendMessage();
		else {
			Scheduler.INSTANCE.schedule(TimeTowerReminder::sendMessage, 60 * 60 * 20 - (int) ((System.currentTimeMillis() - time) / 50)); // 50 milliseconds is 1 tick
			scheduled = true;
		}
	}
}
