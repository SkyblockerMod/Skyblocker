package de.hysky.skyblocker.utils;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUtils {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void recursiveDelete(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return;
		}

		if (Files.isDirectory(dir) && !Files.isSymbolicLink(dir)) {
			try (Stream<Path> stream = Files.list(dir)) {
				stream.forEach(child -> {
					try {
						recursiveDelete(child);
					} catch (Exception e) {
						LOGGER.error("[Skyblocker] Encountered an exception while deleting a file. Path: {}", child.toAbsolutePath(), e);
					}
				});
			}
		}

		if (!Files.isWritable(dir) && !dir.toFile().setWritable(true)) {
			LOGGER.error("[Skyblocker] Failed to make file writable. Path: {}", dir.toAbsolutePath());
		}

		Files.delete(dir);
	}

	/**
	 * Replaces any characters that do not match the regex: [^a-z0-9_.-]
	 *
	 * @implNote Designed to convert a file path to an {@link net.minecraft.util.Identifier}
	 */
	public static String normalizePath(Path path) {
		return path.toString().toLowerCase().replaceAll("[^a-z0-9_.-]", "");
	}
}
