package de.hysky.skyblocker.config.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.logging.LogUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;

/**
 * Handles automatic backups of the main config file.
 */
public class ConfigBackupManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path BACKUP_DIR = SkyblockerMod.CONFIG_DIR.resolve("config_backups");
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	/// Max number of backups to keep per type.
	private static final int MAX_BACKUPS = 20;

	private ConfigBackupManager() {}

	public static void init() {
		CompletableFuture.runAsync(() -> {
			try {
				Files.createDirectories(BACKUP_DIR);
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to create backup directory!", e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);

		ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> backupConfig());
	}

	public static void backupConfig() {
		backupConfig(ConfigType.MAIN);
	}

	public static void backupConfig(ConfigType type) {
		try {
			if (!Files.exists(type.path)) return;
			Files.createDirectories(BACKUP_DIR);

			List<Path> backups = listBackups(type);
			if (!backups.isEmpty() && Files.mismatch(type.path, backups.getFirst()) == -1) {
				return; // current config matches the newest backup
			}

			Path backup = BACKUP_DIR.resolve(type.getBackupFileName());
			Files.copy(type.path, backup, StandardCopyOption.REPLACE_EXISTING);
			cleanOldBackups(type);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to backup config!", e);
		}
	}

	public static List<Path> listBackups() throws IOException {
		return listBackups(ConfigType.MAIN);
	}

	/// List backups with the most recent one first.
	public static List<Path> listBackups(ConfigType type) throws IOException {
		if (!Files.exists(BACKUP_DIR)) return List.of();
		try (Stream<Path> stream = Files.list(BACKUP_DIR)) {
			return stream
					.filter(type::isOfType)
					.sorted(Comparator.reverseOrder())
					.toList();
		}
	}

	public static void restoreBackup(Path backup) throws IOException {
		if (!ConfigType.MAIN.isOfType(backup)) {
			LOGGER.error("[Skyblocker Config Backup Manager] Attempted to restore a backup that is not of type MAIN: {}", backup);
			return;
		}
		Files.copy(backup, SkyblockerConfigManager.getConfigPath(), StandardCopyOption.REPLACE_EXISTING);
		SkyblockerConfigManager.reload();
	}

	private static void cleanOldBackups(ConfigType type) throws IOException {
		List<Path> backups = listBackups(type);
		for (int i = MAX_BACKUPS; i < backups.size(); i++) {
			try {
				Files.deleteIfExists(backups.get(i));
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to delete old backup {}", backups.get(i), e);
			}
		}
	}

	public enum ConfigType {
		MAIN(SkyblockerConfigManager.getConfigPath()),
		HUD_WIDGETS(WidgetManager.FILE);

		private final Path path;
		private final String baseName;
		private final String extension;

		ConfigType(Path path) {
			this.path = path;
			String fileName = path.getFileName().toString();
			this.baseName = FilenameUtils.getBaseName(fileName);
			this.extension = FilenameUtils.getExtension(fileName);
		}

		public boolean isOfType(Path path) {
			String name = path.getFileName().toString();
			return name.startsWith(baseName + "_") && name.endsWith("." + extension);
		}

		public String getBackupFileName() {
			return baseName + "_" + FORMATTER.format(LocalDateTime.now()) + "." + extension;
		}
	}
}
