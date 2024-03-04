package de.hysky.skyblocker.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public class FileUtils {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void recursiveDelete(Path dir) throws IOException {
		if (Files.isDirectory(dir) && !Files.isSymbolicLink(dir)) {
			Files.list(dir).forEach(child -> {
				try {
					recursiveDelete(child);
				} catch (Exception e) {
					LOGGER.error("[Skyblocker] Encountered an exception while deleting a file! Path: {}", child.toAbsolutePath(), e);
				}
			});
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
