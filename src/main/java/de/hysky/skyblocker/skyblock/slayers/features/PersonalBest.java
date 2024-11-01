package de.hysky.skyblocker.skyblock.slayers.features;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class PersonalBest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonalBest.class);
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("SlayerPb.json");
	private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, SlayerInfo>> CACHED_SLAYER_STATS = new Object2ObjectOpenHashMap<>();

	@Init
	public static void init() {
		load();
	}

	private static void load() {
		CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(FILE)) {
				CACHED_SLAYER_STATS.putAll(SlayerInfo.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow());
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Slayer Cache] Failed to load saved slayer data!", e);
			}
		});
	}

	private static void save() {
		CompletableFuture.runAsync(() -> {
			try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
				SkyblockerMod.GSON.toJson(SlayerInfo.SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, CACHED_SLAYER_STATS).getOrThrow(), writer);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Slayer Cache] Failed to save slayer data to cache!", e);
			}
		});
	}

	public static boolean isPersonalBest(String slayer, Duration time) {
		String profileId = Utils.getProfileId();
		Object2ObjectOpenHashMap<String, SlayerInfo> profileData = CACHED_SLAYER_STATS.computeIfAbsent(profileId, _uuid -> new Object2ObjectOpenHashMap<>());

		SlayerInfo currentBest = profileData.get(slayer);
		return currentBest == null || time.toMillis() < currentBest.bestTime().toMillis();
	}

	public static void updateBestTime(String slayerType, Duration duration) {
		String profileId = Utils.getProfileId();
		LocalDateTime now = LocalDateTime.now();

		Object2ObjectOpenHashMap<String, SlayerInfo> profileData = CACHED_SLAYER_STATS.computeIfAbsent(profileId, _uuid -> new Object2ObjectOpenHashMap<>());
		SlayerInfo newInfo = new SlayerInfo(duration, now.toString());

		profileData.put(slayerType, newInfo);
		save();
	}

	public record SlayerInfo(Duration bestTime, String date) {
		public static final Codec<SlayerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("bestTime").forGetter(slayerInfo -> formatTime(slayerInfo.bestTime)),
				Codec.STRING.fieldOf("date").forGetter(SlayerInfo::date)
		).apply(instance, (bestTimeStr, date) -> new SlayerInfo(parseTime(bestTimeStr), date)));

		private static final Codec<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, SlayerInfo>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING,
				Codec.unboundedMap(Codec.STRING, CODEC).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new)
		).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);

		private static String formatTime(Duration duration) {
			double seconds = duration.toMillis() / 1000.0;
			return String.format("%.2fsc", seconds);
		}

		private static Duration parseTime(String formattedTime) {
			double seconds = Double.parseDouble(formattedTime.replace("sc", ""));
			return Duration.ofMillis((long) (seconds * 1000));
		}
	}
}
