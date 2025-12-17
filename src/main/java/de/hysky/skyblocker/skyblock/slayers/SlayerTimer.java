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
	private static final ProfiledData<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerPersonalBest>>> CACHED_SLAYER_STATS = new ProfiledData<>(FILE, SlayerPersonalBest.SERIALIZATION_CODEC);

	@Init
	public static void init() {
		CACHED_SLAYER_STATS.load();
	}

	public static void sendMessage() {
		if (!SkyblockerConfigManager.get().slayers.slainTime) return;

		SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
		if (slayerQuest == null || slayerQuest.bossSpawnTime == null) return;
		Instant bossDeathTime = slayerQuest.bossDeathTime != null ? slayerQuest.bossDeathTime : Instant.now();

		long currentPBMills = getPersonalBest(slayerQuest);
		long newPBMills = Duration.between(slayerQuest.bossSpawnTime, bossDeathTime).toMillis();

		String currentPB = formatTime(currentPBMills);
		String newPB = formatTime(newPBMills);

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

			updateBestTime(slayerQuest, newPBMills);
		} else {
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.slainTime", Text.literal(newPB).formatted(Formatting.YELLOW))), false);
			if (currentPBMills == -1) {
				updateBestTime(slayerQuest, newPBMills);
			}
		}
	}

	private static long getPersonalBest(SlayerManager.SlayerQuest slayerQuest) {
		var profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		if (profileData != null) {
			var typeData = profileData.computeIfAbsent(slayerQuest.slayerType, _type -> new Object2ObjectOpenHashMap<>());
			SlayerPersonalBest currentBest = typeData.get(slayerQuest.slayerTier);
			//noinspection ConstantConditions
			return currentBest != null ? currentBest.bestTimeMillis() : -1;
		}

		return -1;
	}

	private static void updateBestTime(SlayerManager.SlayerQuest slayerQuest, long timeElapsed) {
		var profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		if (profileData != null) {
			var typeData = profileData.computeIfAbsent(slayerQuest.slayerType, _type -> new Object2ObjectOpenHashMap<>());
			typeData.put(slayerQuest.slayerTier, new SlayerPersonalBest(timeElapsed, System.currentTimeMillis()));
			CACHED_SLAYER_STATS.save();
		}
	}

	private static String formatTime(long millis) {
		return String.format("%.2fs", millis / 1000.0);
	}

	private record SlayerPersonalBest(long bestTimeMillis, long dateMillis) {
		private static final Codec<SlayerPersonalBest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("bestTimeMillis").forGetter(SlayerPersonalBest::bestTimeMillis),
				Codec.LONG.fieldOf("dateMillis").forGetter(SlayerPersonalBest::dateMillis)
		).apply(instance, SlayerPersonalBest::new));

		private static final Codec<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerPersonalBest>>> SERIALIZATION_CODEC = Codec.unboundedMap(SlayerType.CODEC,
				Codec.unboundedMap(SlayerTier.CODEC, CODEC).xmap(Object2ObjectOpenHashMap::new, Function.identity())
		).xmap(Object2ObjectOpenHashMap::new, Function.identity());
	}
}
