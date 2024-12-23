package de.hysky.skyblocker.skyblock.slayers;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SlayerTimer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SlayerTimer.class);
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("SlayerPb.json");
	private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>>> CACHED_SLAYER_STATS = new Object2ObjectOpenHashMap<>();

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

	public static void onBossDeath(Instant startTime) {
		if (!SkyblockerConfigManager.get().slayers.slainTime || startTime == null) return;
		Instant slainTime = Instant.now();
		long timeElapsed = Duration.between(startTime, slainTime).toMillis();
		String duration = formatTime(timeElapsed);

		long currentPB = getPersonalBest(SlayerManager.getSlayerType(), SlayerManager.getSlayerTier());

		if (currentPB != -1 && (currentPB > timeElapsed)) {
			MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.slainTime", Text.literal(duration).formatted(Formatting.YELLOW)).append(" ").append(Text.translatable("skyblocker.slayer.personalBest").formatted(Formatting.LIGHT_PURPLE))), false);
			MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.previousPB", Text.literal(formatTime(currentPB)).formatted(Formatting.YELLOW))), false);
			updateBestTime(SlayerManager.getSlayerType(), SlayerManager.getSlayerTier(), timeElapsed);
		} else {
			MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.slainTime", Text.literal(duration).formatted(Formatting.YELLOW))), false);
			if (currentPB == -1) {
				updateBestTime(SlayerManager.getSlayerType(), SlayerManager.getSlayerTier(), timeElapsed);
			}
		}
	}

	private static long getPersonalBest(SlayerType slayerType, SlayerTier slayerTier) {
		String profileId = Utils.getProfileId();
		Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>> profileData = CACHED_SLAYER_STATS.computeIfAbsent(profileId, _uuid -> new Object2ObjectOpenHashMap<>());
		Object2ObjectOpenHashMap<SlayerTier, SlayerInfo> typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());

		SlayerInfo currentBest = typeData.get(slayerTier);
		return currentBest != null ? currentBest.bestTimeMillis : -1;
	}

	private static void updateBestTime(SlayerType slayerType, SlayerTier slayerTier, long timeElapsed) {
		String profileId = Utils.getProfileId();
		long nowMillis = System.currentTimeMillis();

		Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>> profileData = CACHED_SLAYER_STATS.computeIfAbsent(profileId, _uuid -> new Object2ObjectOpenHashMap<>());
		Object2ObjectOpenHashMap<SlayerTier, SlayerInfo> typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());
		SlayerInfo newInfo = new SlayerInfo(timeElapsed, nowMillis);

		typeData.put(slayerTier, newInfo);
		save();
	}

	private static String formatTime(long millis) {
		return String.format("%.2fs", millis / 1000.0);
	}

	public record SlayerInfo(long bestTimeMillis, long dateMillis) {
		public static final Codec<SlayerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("bestTimeMillis").forGetter(SlayerInfo::bestTimeMillis),
				Codec.LONG.fieldOf("dateMillis").forGetter(SlayerInfo::dateMillis)
		).apply(instance, SlayerInfo::new));

		private static final Codec<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING,
				Codec.unboundedMap(SlayerType.CODEC,
						Codec.unboundedMap(SlayerTier.CODEC, CODEC).xmap(Object2ObjectOpenHashMap::new, Function.identity())
				).xmap(Object2ObjectOpenHashMap::new, Function.identity())
		).xmap(Object2ObjectOpenHashMap::new, Function.identity());
	}
}
