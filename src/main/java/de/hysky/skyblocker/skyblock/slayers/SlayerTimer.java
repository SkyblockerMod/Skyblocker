package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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

	public static void onBossDeath(SlayerManager.BossFight bossFight) {
		if (!SkyblockerConfigManager.get().slayers.slainTime || bossFight.sentTime) return;
		bossFight.sentTime = true;

		SlayerType slayerType = SlayerManager.getSlayerType();
		SlayerTier slayerTier = SlayerManager.getSlayerTier();
		if (slayerType == null || slayerTier == null || slayerType.isUnknown() || slayerTier.isUnknown()) return;

		long newPBMills = Duration.between(bossFight.bossSpawnTime, Instant.now()).toMillis();
		String newPB = formatTime(newPBMills);

		long currentPBMills = getPersonalBest(slayerType, slayerTier);
		String currentPB = formatTime(currentPBMills);

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		assert player != null;
		if (currentPBMills != -1 && currentPBMills > newPBMills) {
			player.sendMessage(Constants.PREFIX.get().append(
					Text.translatable("skyblocker.slayer.slainTime", Text.literal(newPB).formatted(Formatting.YELLOW))
							.append(" ")
							.append(Text.translatable("skyblocker.slayer.personalBest").formatted(Formatting.LIGHT_PURPLE))), false);
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.previousPersonalBest", Text.literal(currentPB).formatted(Formatting.YELLOW))), false);

			TitleContainer.addTitleAndPlaySound(new Title("skyblocker.slayer.personalBest", Formatting.AQUA), 100);
			TitleContainer.addTitle(new Title(
					Text.literal(currentPB).formatted(Formatting.YELLOW)
							.append(Text.literal(" ➜ ").formatted(Formatting.DARK_AQUA))
							.append(Text.literal(newPB).formatted(Formatting.GREEN))), 100);

			updateBestTime(slayerType, slayerTier, newPBMills);
		} else {
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.slainTime", Text.literal(newPB).formatted(Formatting.YELLOW))), false);
			if (currentPBMills == -1) {
				updateBestTime(slayerType, slayerTier, newPBMills);
			}
		}
	}

	private static long getPersonalBest(SlayerType slayerType, SlayerTier slayerTier) {
		Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>> profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		Object2ObjectOpenHashMap<SlayerTier, SlayerInfo> typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());

		SlayerInfo currentBest = typeData.get(slayerTier);
		return currentBest != null ? currentBest.bestTimeMillis() : -1;
	}

	private static void updateBestTime(SlayerType slayerType, SlayerTier slayerTier, long timeElapsed) {
		Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>> profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		Object2ObjectOpenHashMap<SlayerTier, SlayerInfo> typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());
		SlayerInfo newInfo = new SlayerInfo(timeElapsed, System.currentTimeMillis());

		typeData.put(slayerTier, newInfo);
		CACHED_SLAYER_STATS.save();
	}

	private static String formatTime(long millis) {
		return String.format("%.2fs", millis / 1000.0);
	}

	private record SlayerInfo(long bestTimeMillis, long dateMillis) {
		private static final Codec<SlayerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("bestTimeMillis").forGetter(SlayerInfo::bestTimeMillis),
				Codec.LONG.fieldOf("dateMillis").forGetter(SlayerInfo::dateMillis)
		).apply(instance, SlayerInfo::new));

		private static final Codec<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerInfo>>> SERIALIZATION_CODEC = Codec.unboundedMap(SlayerType.CODEC,
				Codec.unboundedMap(SlayerTier.CODEC, CODEC).xmap(Object2ObjectOpenHashMap::new, Function.identity())
		).xmap(Object2ObjectOpenHashMap::new, Function.identity());
	}
}
