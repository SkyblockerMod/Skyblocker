package de.hysky.skyblocker.skyblock.dungeon.roomPreview;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelInfo;
import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class RoomPreviewServer {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final String SAVE_NAME = "skyblocker-room-preview";

	static boolean isActive = false;
	static String selectedRoom = "";
	static List<Text> errorMessages = new ArrayList<>();

	@Init
	public static void init() {
		ServerPlayerEvents.JOIN.register(RoomPreviewServer::onPlayerJoin);
		ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> applyNightVision(newP));
		ServerLifecycleEvents.SERVER_STOPPING.register((server) -> RoomPreviewServer.reset());
	}

	public static void onPlayerJoin(ServerPlayerEntity player) {
		if (!isActive) return;
		Scheduler.INSTANCE.schedule(RoomPreview::onJoin, 5);
		applyNightVision(player);
		for (Text msg : errorMessages) {
			player.sendMessage(msg);
		}
		errorMessages.clear();
	}

	public static void applyNightVision(ServerPlayerEntity player) {
		if (!isActive) return;
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 999999999, 1, false, false));
	}

	private static void reset() {
		isActive = false;
		selectedRoom = "";
	}

	public static void createServer() {
		var previousSave = CLIENT.getLevelStorage().resolve(SAVE_NAME).toFile();
		FileUtils.deleteQuietly(previousSave);

		var gameRules = new GameRules(DataConfiguration.SAFE_MODE.enabledFeatures());
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
		gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, null);

		CLIENT.createIntegratedServerLoader().createAndStart(SAVE_NAME,
				new LevelInfo(SAVE_NAME, GameMode.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, DataConfiguration.SAFE_MODE),
				new GeneratorOptions("skyblocker".hashCode(), false, false),
				(lookup) -> {
					var preset = WorldPresets.createTestOptions(lookup);
					var config = new FlatChunkGeneratorConfig(Optional.empty(), lookup.getOrThrow(RegistryKeys.BIOME).getOrThrow(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla("the_void"))), List.of());
					return preset.with(lookup, new FlatChunkGenerator(config));
				},
				null
		);
	}

	public static void setupServer() {
		IntegratedServer server = CLIENT.getServer();
		if (server == null) return;
		isActive = true;
	}

	public static void addErrorMessage(Text errorText) {
		errorMessages.add(Constants.PREFIX.get().append(errorText));
	}

	public static void loadRoom(String type, String roomName) {
		IntegratedServer server = CLIENT.getServer();
		if (server == null) return;
		Optional<int[]> blockData = DungeonManager.getRoomBlockData(type, roomName);
		if (blockData.isEmpty()) {
			addErrorMessage(Text.literal("Failed to load room: invalid room!").formatted(Formatting.RED));
			return;
		}

		selectedRoom = roomName;
		var template = server.getStructureTemplateManager().createTemplate(RoomStructure.getCompound(blockData.get()));
		server.executeAsync(future ->
				template.place(server.getOverworld(), BlockPos.ORIGIN, BlockPos.ORIGIN, new StructurePlacementData(), server.getOverworld().random, 0));

		// Add a world border to partially prevent falling out of the world
		server.execute(() -> {
			var border = server.getOverworld().getWorldBorder();
			border.setCenter(((double) template.getSize().getX() + 1) / 2, ((double) template.getSize().getZ() + 1) / 2);
			border.setSize(Math.max(template.getSize().getX(), template.getSize().getZ()));
		});
	}
}
