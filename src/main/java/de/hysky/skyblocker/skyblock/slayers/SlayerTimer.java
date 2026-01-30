package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.data.ProfiledData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SlayerTimer {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("slayer_personal_best.json");
	private static final ProfiledData<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>>> CACHED_SLAYER_STATS = new ProfiledData<>(FILE, SlayerInfo.SERIALIZATION_CODEC);

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
			Minecraft.getInstance().player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.slainTime", Component.literal(duration).withStyle(ChatFormatting.YELLOW)).append(" ").append(Component.translatable("skyblocker.slayer.personalBest").withStyle(ChatFormatting.LIGHT_PURPLE))), false);
			Minecraft.getInstance().player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.previousPB", Component.literal(formatTime(currentPB)).withStyle(ChatFormatting.YELLOW))), false);
			updateBestTime(SlayerManager.getSlayerType(), SlayerManager.getSlayerTier(), timeElapsed);
		} else {
			Minecraft.getInstance().player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.slainTime", Component.literal(duration).withStyle(ChatFormatting.YELLOW))), false);
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
