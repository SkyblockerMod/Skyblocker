package de.hysky.skyblocker.skyblock.dungeon.preview;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class RoomPreviewServer {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String SAVE_NAME = "skyblocker-room-preview";

	public static boolean isActive = false;
	static String selectedRoom = "";
	static List<Component> errorMessages = new ArrayList<>();

	@Init
	public static void init() {
		ServerPlayerEvents.JOIN.register(RoomPreviewServer::onPlayerJoin);
		ServerPlayerEvents.AFTER_RESPAWN.register((_oldP, newP, _alive) -> applyNightVision(newP));
		ServerLifecycleEvents.SERVER_STARTED.register(RoomPreviewServer::checkServer);
		ServerLifecycleEvents.SERVER_STOPPING.register((_server) -> RoomPreviewServer.reset());
	}

	public static void onPlayerJoin(ServerPlayer player) {
		if (!isActive) return;
		Scheduler.INSTANCE.schedule(RoomPreview::onJoin, 5);
		applyNightVision(player);
		player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.roomPreview.joinMessage", selectedRoom).getString()));
		for (Component msg : errorMessages) {
			player.sendSystemMessage(msg);
		}
		errorMessages.clear();
	}

	public static void applyNightVision(ServerPlayer player) {
		if (!isActive) return;
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 1, false, false));
	}

	private static void checkServer(MinecraftServer server) {
		if (isActive) return;
		if (!server.getWorldData().getLevelName().equals(SAVE_NAME)) return;

		CompoundTag previewData = server.getCommandStorage().get(SkyblockerMod.id(SAVE_NAME));
		if (previewData.isEmpty()) return;
		isActive = server.getCommandStorage().get(SkyblockerMod.id(SAVE_NAME)).getBooleanOr("isActive", false);
		selectedRoom = server.getCommandStorage().get(SkyblockerMod.id(SAVE_NAME)).getStringOr("selectedRoom", "");
	}

	private static void reset() {
		isActive = false;
		selectedRoom = "";
		errorMessages.clear();
	}

	public static void createServer() {
		File previousSave = CLIENT.getLevelSource().getLevelPath(SAVE_NAME).toFile();
		FileUtils.deleteQuietly(previousSave);

		GameRules gameRules = new GameRules(WorldDataConfiguration.DEFAULT.enabledFeatures());
		gameRules.set(GameRules.ADVANCE_TIME, false, null);
		gameRules.set(GameRules.RANDOM_TICK_SPEED, 0, null);

		isActive = true;
		CLIENT.createWorldOpenFlows().createFreshLevel(SAVE_NAME,
				new LevelSettings(SAVE_NAME, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, WorldDataConfiguration.DEFAULT),
				new WorldOptions(SAVE_NAME.hashCode(), false, false),
				(lookup) -> {
					var preset = WorldPresets.createFlatWorldDimensions(lookup);
					var config = new FlatLevelGeneratorSettings(Optional.empty(), lookup.lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace("the_void"))), List.of());
					return preset.replaceOverworldGenerator(lookup, new FlatLevelSource(config));
				},
				new TitleScreen()
		);

		IntegratedServer server = CLIENT.getSingleplayerServer();
		if (server == null) reset();
	}

	public static void addErrorMessage(Component errorText) {
		errorMessages.add(Constants.PREFIX.get().append(errorText.getString()));
	}

	public static @Nullable StructureTemplate getStructureTemplate(IntegratedServer server, String type, String roomName) {
		Optional<int[]> blockData = DungeonManager.getRoomBlockData(type, roomName);
		return blockData.map(blocks -> server.getStructureManager().readStructure(RoomStructure.getCompound(blocks))).orElse(null);
	}

	public static void loadRoom(String type, String roomName) {
		IntegratedServer server = CLIENT.getSingleplayerServer();
		if (server == null) return;

		selectedRoom = roomName;
		StructureTemplate template = getStructureTemplate(server, type, roomName);
		if (template == null) {
			addErrorMessage(Component.translatable("skyblocker.dungeons.roomPreview.failedToLoad", Component.translatable("skyblocker.dungeons.roomPreview.invalidRoom")).withStyle(ChatFormatting.RED));
			return;
		}

		server.execute(() ->
				template.placeInWorld(server.overworld(), BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings(), server.overworld().random, 0));

		// Add a world border to partially prevent falling out of the world
		server.execute(() -> {
			WorldBorder border = server.overworld().getWorldBorder();
			border.setCenter(((double) template.getSize().getX() + 1) / 2, ((double) template.getSize().getZ() + 1) / 2);
			border.setSize(Math.max(template.getSize().getX(), template.getSize().getZ()));
		});

		// Save room preview data
		server.execute(() -> {
			CompoundTag previewData = new CompoundTag();
			previewData.putBoolean("isActive", true);
			previewData.putString("selectedRoom", roomName);
			server.getCommandStorage().set(SkyblockerMod.id(SAVE_NAME), previewData);
		});
	}
}
