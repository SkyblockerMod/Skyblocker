package de.hysky.skyblocker.skyblock.slayers.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerTier;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SlayerTimer {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("slayer_personal_best.json");
	private static final ProfiledData<Object2ObjectOpenHashMap<SlayerType, Object2ObjectOpenHashMap<SlayerTier, SlayerPersonalBest>>> CACHED_SLAYER_STATS = new ProfiledData<>(FILE, SlayerPersonalBest.SERIALIZATION_CODEC);

	@Init
	public static void init() {
		CACHED_SLAYER_STATS.load();
		ClientCommandRegistrationCallback.EVENT.register(SlayerTimer::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandBuildContext) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("slayers")
				.then(literal("revenant").executes(context -> SlayerTimer.sendSlayerPersonalBest(context, SlayerType.REVENANT)))
				.then(literal("tarantula").executes(context -> SlayerTimer.sendSlayerPersonalBest(context, SlayerType.TARANTULA)))
				.then(literal("sven").executes(context -> SlayerTimer.sendSlayerPersonalBest(context, SlayerType.SVEN)))
				.then(literal("voidgloom").executes(context -> SlayerTimer.sendSlayerPersonalBest(context, SlayerType.VOIDGLOOM)))
				.then(literal("demonlord").executes(context -> SlayerTimer.sendSlayerPersonalBest(context, SlayerType.DEMONLORD)))
				.then(literal("vampire").executes(context -> SlayerTimer.sendSlayerPersonalBest(context, SlayerType.VAMPIRE)))
		));
	}

	private static int sendSlayerPersonalBest(CommandContext<FabricClientCommandSource> context, SlayerType slayerType) {
		FabricClientCommandSource source = context.getSource();

		SlayerTier[] tiers = SlayerTier.values();
		for (int i = tiers.length - 1; i >= 0; i--) {
			SlayerTier slayerTier = tiers[i];
			long time = getPersonalBest(slayerType, slayerTier);

			if (time != -1) {
				MutableComponent bossText = Component.literal(slayerType.bossName + " " + slayerTier.name()).withStyle(ChatFormatting.DARK_PURPLE);
				MutableComponent timeText = Component.literal(formatTime(time)).withStyle(ChatFormatting.AQUA);
				source.sendFeedback(Constants.PREFIX.get().append(bossText.append(": ").append(timeText)));
				return Command.SINGLE_SUCCESS;
			}
		}

		source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.personalBestMissing", slayerType.bossName)));
		return 0;
	}

	public static void sendMessage() {
		if (!SkyblockerConfigManager.get().slayers.slainTime) return;

		SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
		if (slayerQuest == null || slayerQuest.bossSpawnTime == null) return;
		Instant bossDeathTime = slayerQuest.bossDeathTime != null ? slayerQuest.bossDeathTime : Instant.now();

		long currentPBMills = getPersonalBest(slayerQuest.slayerType, slayerQuest.slayerTier);
		long newPBMills = Duration.between(slayerQuest.bossSpawnTime, bossDeathTime).toMillis();

		String currentPB = formatTime(currentPBMills);
		String newPB = formatTime(newPBMills);

		LocalPlayer player = Minecraft.getInstance().player;
		assert player != null;
		if (currentPBMills != -1 && currentPBMills > newPBMills) {
			player.displayClientMessage(Constants.PREFIX.get().append(
					Component.translatable("skyblocker.slayer.slainTime", Component.literal(newPB).withStyle(ChatFormatting.YELLOW))
							.append(" ")
							.append(Component.translatable("skyblocker.slayer.personalBest").withStyle(ChatFormatting.LIGHT_PURPLE))), false);
			player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.previousPersonalBest", Component.literal(currentPB).withStyle(ChatFormatting.YELLOW))), false);

			TitleContainer.addTitleAndPlaySound(new Title("skyblocker.slayer.personalBest", ChatFormatting.AQUA), 100);
			TitleContainer.addTitle(new Title(
					Component.literal(currentPB).withStyle(ChatFormatting.YELLOW)
							.append(Component.literal(" ➜ ").withStyle(ChatFormatting.DARK_AQUA))
							.append(Component.literal(newPB).withStyle(ChatFormatting.GREEN))), 100);

			updateBestTime(slayerQuest, newPBMills);
		} else {
			player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.slainTime", Component.literal(newPB).withStyle(ChatFormatting.YELLOW))), false);
			if (currentPBMills == -1) {
				updateBestTime(slayerQuest, newPBMills);
			}
		}
	}

	private static long getPersonalBest(SlayerType slayerType, SlayerTier slayerTier) {
		var profileData = CACHED_SLAYER_STATS.computeIfAbsent(Object2ObjectOpenHashMap::new);
		if (profileData != null) {
			var typeData = profileData.computeIfAbsent(slayerType, _type -> new Object2ObjectOpenHashMap<>());
			SlayerPersonalBest currentBest = typeData.get(slayerTier);
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
