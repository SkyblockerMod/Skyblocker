package de.hysky.skyblocker.config.backup;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Handles automatic backups of the main config file.
 */
public class ConfigBackupManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path BACKUP_DIR = SkyblockerMod.CONFIG_DIR.resolve("config_backups");
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private static final int MAX_BACKUPS = 10;

	private ConfigBackupManager() {}

	public static void init() {
		CompletableFuture.runAsync(() -> {
			try {
				Files.createDirectories(BACKUP_DIR);
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to create backup directory!", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> backupConfig());
	}

	public static void backupConfig() {
		try {
			Path configPath = SkyblockerConfigManager.getConfigPath();
			if (!Files.exists(configPath)) return;
			Files.createDirectories(BACKUP_DIR);

			List<Path> backups = listBackups();
			Path latest = backups.isEmpty() ? null : backups.getFirst();
			if (latest != null && Files.mismatch(configPath, latest) == -1) {
				return; // current config matches the newest backup
			}

			Path backup = BACKUP_DIR.resolve("skyblocker_" + FORMATTER.format(LocalDateTime.now()) + ".json");
			Files.copy(configPath, backup, StandardCopyOption.REPLACE_EXISTING);
			cleanOldBackups();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to backup config!", e);
		}
	}

	public static List<Path> listBackups() throws IOException {
		if (!Files.exists(BACKUP_DIR)) return List.of();
		try (Stream<Path> stream = Files.list(BACKUP_DIR)) {
			return stream
					.filter(p -> p.getFileName().toString().endsWith(".json"))
					.sorted(Comparator.reverseOrder())
					.toList();
		}
	}

	public static void restoreBackup(Path backup) throws IOException {
		Files.copy(backup, SkyblockerConfigManager.getConfigPath(), StandardCopyOption.REPLACE_EXISTING);
		SkyblockerConfigManager.reload();
	}

	private static void cleanOldBackups() throws IOException {
		List<Path> backups = listBackups();
		for (int i = MAX_BACKUPS; i < backups.size(); i++) {
			try {
				Files.deleteIfExists(backups.get(i));
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to delete old backup {}", backups.get(i), e);
			}
		}
	}
}
