package de.hysky.skyblocker.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.FileUtils;
import de.hysky.skyblocker.utils.Http;

public class ImageRepoLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	static final Path REPO_DIRECTORY = SkyblockerMod.CONFIG_DIR.resolve("image-repo");
	private static final String BRANCH_INFO = "https://api.github.com/repos/SkyblockerMod/Skyblocker-Assets/branches/images";
	private static final String REPO_DOWNLOAD = "https://github.com/SkyblockerMod/Skyblocker-Assets/archive/refs/heads/images.zip";
	private static final String PLACEHOLDER_HASH = "None!";

	public static void init() {
		update(0);
	}

	/**
	 * Attempts to update/load the image repository, if any errors are encountered it will try 3 times.
	 */
	private static void update(int retries) {
		CompletableFuture.runAsync(() -> {
			if (retries < 3) {
				try {
					long start = System.currentTimeMillis();
					//Retrieve the saved commit hash
					String savedCommitHash = checkSavedCommitData();

					//Fetch the latest commit data
					JsonObject response = JsonParser.parseString(Http.sendGetRequest(BRANCH_INFO)).getAsJsonObject();
					String latestCommitHash = response.getAsJsonObject("commit").get("sha").getAsString();

					//Download the repository if there was a new commit
					if (!savedCommitHash.equals(latestCommitHash)) {
						InputStream in = Http.downloadContent(REPO_DOWNLOAD);

						//Delete all directories to clear potentially now unused/old files
						//TODO change this to only delete periodically?
						if (Files.exists(REPO_DIRECTORY)) deleteDirectories();

						try (ZipInputStream zis = new ZipInputStream(in)) {
							ZipEntry entry;

							while ((entry = zis.getNextEntry()) != null) {
								Path outputFile = REPO_DIRECTORY.resolve(entry.getName());

								if (entry.isDirectory()) {
									Files.createDirectories(outputFile);
								} else {
									Files.createDirectories(outputFile.getParent());
									Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING);
								}
							}
						}

						writeCommitData(latestCommitHash);

						long end = System.currentTimeMillis();
						LOGGER.info("[Skyblocker] Successfully updated the Image Respository in {} ms! {} → {}", end - start, savedCommitHash, latestCommitHash);
					} else {
						LOGGER.info("[Skyblocker] The Image Respository is up to date!");
					}
				} catch (Exception e) {
					LOGGER.error("[Skyblocker] Error while downloading image repo on attempt {}!", retries, e);
					update(retries + 1);
				}
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	/**
	 * @return The stored hash or the {@link #PLACEHOLDER_HASH}.
	 */
	private static String checkSavedCommitData() throws IOException {
		Path file = REPO_DIRECTORY.resolve("image_repo.json");

		if (Files.exists(file)) {
			try (BufferedReader reader = Files.newBufferedReader(file)) {
				CommitData commitData = CommitData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();

				return commitData.commit();
			}
		}

		return PLACEHOLDER_HASH;
	}

	/**
	 * Writes the {@code newHash} into a file to be used to check for repo updates.
	 * 
	 * @implNote Checking whether the directory exists or not isn't needed as this is called after all files are written successfully.
	 */
	private static void writeCommitData(String newHash) throws IOException {
		Path file = REPO_DIRECTORY.resolve("image_repo.json");
		CommitData commitData = new CommitData(newHash, System.currentTimeMillis());

		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			SkyblockerMod.GSON.toJson(CommitData.CODEC.encodeStart(JsonOps.INSTANCE, commitData).getOrThrow(), writer);
		}
	}

	/**
	 * Deletes all directories (not files) inside of the {@link #REPO_DIRECTORY}
	 * @throws IOException 
	 */
	private static void deleteDirectories() throws IOException {
		Files.list(REPO_DIRECTORY)
				.filter(Files::isDirectory)
				.forEach(dir -> {
					try {
						FileUtils.recursiveDelete(dir);
					} catch (Exception e) {
						LOGGER.error("[Skyblocker] Encountered an exception while deleting a directory! Path: {}", dir.toAbsolutePath(), e);
					}
				});
	}

	record CommitData(String commit, long lastUpdated) {
		static final Codec<CommitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("commit").forGetter(CommitData::commit),
				Codec.LONG.fieldOf("lastUpdated").forGetter(CommitData::lastUpdated))
				.apply(instance, CommitData::new));		
	}
}
