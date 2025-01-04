package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

public class SlayerTimer {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("slayer_personal_best.json");
	private static final ProfiledData<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>>> CACHED_SLAYER_STATS = new ProfiledData<>(FILE, SlayerInfo.SERIALIZATION_CODEC, true, true);

	@Init
	public static void init() {
		CACHED_SLAYER_STATS.load();
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
		Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>> profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		Object2ObjectOpenHashMap<SlayerTier, SlayerInfo> typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());

		SlayerInfo currentBest = typeData.get(slayerTier);
		return currentBest != null ? currentBest.bestTimeMillis : -1;
	}

	private static void updateBestTime(SlayerType slayerType, SlayerTier slayerTier, long timeElapsed) {
		long nowMillis = System.currentTimeMillis();

		Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>> profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		Object2ObjectOpenHashMap<SlayerTier, SlayerInfo> typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());
		SlayerInfo newInfo = new SlayerInfo(timeElapsed, nowMillis);

		typeData.put(slayerTier, newInfo);
		CACHED_SLAYER_STATS.save();
	}

	private static String formatTime(long millis) {
		return String.format("%.2fs", millis / 1000.0);
	}

	public record SlayerInfo(long bestTimeMillis, long dateMillis) {
		public static final Codec<SlayerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("bestTimeMillis").forGetter(SlayerInfo::bestTimeMillis),
				Codec.LONG.fieldOf("dateMillis").forGetter(SlayerInfo::dateMillis)
		).apply(instance, SlayerInfo::new));

		private static final Codec<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>>> SERIALIZATION_CODEC = Codec.unboundedMap(SlayerType.CODEC,
				Codec.unboundedMap(SlayerTier.CODEC, CODEC).xmap(Object2ObjectOpenHashMap::new, Function.identity())
		).xmap(Object2ObjectOpenHashMap::new, Function.identity());
	}
}
