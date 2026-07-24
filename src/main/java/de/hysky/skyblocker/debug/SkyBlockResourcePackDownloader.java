package de.hysky.skyblocker.debug;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.FileUtils;
import de.hysky.skyblocker.utils.Http;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public class SkyBlockResourcePackDownloader {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int CURRENT_RESOURCE_PACK_VERSION = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major();
	private static final String HYPIXEL_RESOURCE_PACKS_API = "https://api.hypixel.net/v2/resources/packs";
	private static final String SKYBLOCK_RESOURCE_PACK_ID = "SkyBlock";

	@Init
	public static void init() {
		if (Debug.debugEnabled() && SkyblockerConfigManager.get().debug.skyblockResourcePack.downloadResourcePack) {
			downloadResourcePack();
		}
	}

	public static void downloadResourcePack() {
		CompletableFuture.runAsync(SkyBlockResourcePackDownloader::downloadResourcePackInternal, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static boolean downloadResourcePackInternal() {
		try {
			Path resourcePackDestination = Minecraft.getInstance().getResourcePackDirectory().resolve("Hypixel SkyBlock");
			String packsResponse = Http.sendGetRequest(HYPIXEL_RESOURCE_PACKS_API);
			List<HypixelPack> packs = HypixelPack.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(packsResponse).getAsJsonObject().get("packs")).getOrThrow();
			HypixelPack skyblockResourcePack = packs.stream()
					.filter(pack -> pack.id().equals(SKYBLOCK_RESOURCE_PACK_ID))
					.findFirst()
					.get();
			boolean shouldUpdatePack = !Files.exists(resourcePackDestination) || skyblockResourcePack.lastUpdated() > Files.getLastModifiedTime(resourcePackDestination).toMillis();
			Optional<String> packUrl = skyblockResourcePack.versions().stream()
					.filter(version -> version.packFormat() == CURRENT_RESOURCE_PACK_VERSION)
					.map(HypixelPackVersion::url)
					.findFirst();

			if (shouldUpdatePack && packUrl.isPresent()) {
				// Clear the directory to ensure there aren't any leftover files from previous versions
				FileUtils.recursiveDelete(resourcePackDestination);

				InputStream packInputStream = Http.downloadContent(packUrl.get());
				extractResourcePack(resourcePackDestination, packInputStream);
				Files.setLastModifiedTime(resourcePackDestination, FileTime.fromMillis(skyblockResourcePack.lastUpdated()));
				LOGGER.info("[Skyblocker Resource Pack Downloader] Successfully downloaded the SkyBlock pack!");

				return true;
			} else if (packUrl.isEmpty()) {
				LOGGER.warn("[Skyblocker Resource Pack Downloader] Could not find a version of the SkyBlock pack for format {}.", CURRENT_RESOURCE_PACK_VERSION);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Resource Pack Downloader] Could not download the SkyBlock pack.", e);
		}

		return false;
	}

	private static void extractResourcePack(Path packDirectory, InputStream packInputStream) throws Exception {
		try (ZipInputStream zipInputStream = new ZipInputStream(packInputStream)) {
			ZipEntry entry;

			while ((entry = zipInputStream.getNextEntry()) != null) {
				Path outputPath = packDirectory.resolve(entry.getName()).normalize();

				// Ensure that the zip cannot try to write to a file outside of the pack directory (path traversal exploit)
				if (!outputPath.startsWith(packDirectory)) {
					throw new IllegalStateException("Malicious ZIP entry detected: " + entry.getName());
				}

				if (entry.isDirectory()) {
					Files.createDirectories(outputPath);
				} else {
					Files.createDirectories(outputPath.getParent());
					Files.copy(zipInputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
				}

				zipInputStream.closeEntry();
			}
		}
	}

	private record HypixelPack(String id, long lastUpdated, String deployId, List<HypixelPackVersion> versions) {
		private static final Codec<HypixelPack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(HypixelPack::id),
				Codec.LONG.fieldOf("lastUpdated").forGetter(HypixelPack::lastUpdated),
				Codec.STRING.fieldOf("deployId").forGetter(HypixelPack::deployId),
				HypixelPackVersion.CODEC.listOf().fieldOf("versions").forGetter(HypixelPack::versions))
				.apply(instance, HypixelPack::new));
		private static final Codec<List<HypixelPack>> LIST_CODEC = CODEC.listOf();
	}

	private record HypixelPackVersion(int packFormat, String hash, String url) {
		private static final Codec<HypixelPackVersion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("packFormat").forGetter(HypixelPackVersion::packFormat),
				Codec.STRING.fieldOf("hash").forGetter(HypixelPackVersion::hash),
				Codec.STRING.fieldOf("url").forGetter(HypixelPackVersion::url))
				.apply(instance, HypixelPackVersion::new));
	}
}
