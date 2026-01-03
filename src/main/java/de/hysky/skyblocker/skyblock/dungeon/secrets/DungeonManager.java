package de.hysky.skyblocker.skyblock.dungeon.secrets;

import static de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils.getColor;
import static de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils.getMapPosForNWMostRoom;
import static de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils.getPhysicalPosFromMap;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.VisibleForTesting;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMap;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DungeonManager {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	protected static final Logger LOGGER = LoggerFactory.getLogger(DungeonManager.class);

	@VisibleForTesting
	public static final String DUNGEONS_PATH = "dungeons";
	private static final Path CUSTOM_WAYPOINTS_DIR = SkyblockerMod.CONFIG_DIR.resolve("custom_secret_waypoints.json");

	private static final Pattern KEY_FOUND = Pattern.compile("^RIGHT CLICK on (?:the BLOOD DOOR|a WITHER door) to open it. This key can only be used to open 1 door!$");
	private static final Pattern WITHER_DOOR_OPENED = Pattern.compile("^\\w+ opened a WITHER door!$");
	private static final String BLOOD_DOOR_OPENED = "The BLOOD DOOR has been opened!";
	private static final Pattern TEAM_SCORE_PATTERN = Pattern.compile(" +Team Score: [0-9]+ \\([A-z+]+\\)");

	protected static final float[] RED_COLOR_COMPONENTS = {1, 0, 0};
	protected static final float[] GREEN_COLOR_COMPONENTS = {0, 1, 0};
	/**
	 * Maps the block identifier string to a custom numeric block id used in dungeon rooms data.
	 *
	 * @implNote Not using {@link Registry#getKey(Object) Registry#getId(Block)} and {@link Blocks Blocks} since this is also used by {@link de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU}, which runs outside of Minecraft.
	 */
	@SuppressWarnings("JavadocReference")
	protected static final Object2ByteMap<String> NUMERIC_ID = Object2ByteMap.ofEntries(
			Object2ByteMap.entry("minecraft:stone", (byte) 1),
			Object2ByteMap.entry("minecraft:diorite", (byte) 2),
			Object2ByteMap.entry("minecraft:polished_diorite", (byte) 3),
			Object2ByteMap.entry("minecraft:andesite", (byte) 4),
			Object2ByteMap.entry("minecraft:polished_andesite", (byte) 5),
			Object2ByteMap.entry("minecraft:grass_block", (byte) 6),
			Object2ByteMap.entry("minecraft:dirt", (byte) 7),
			Object2ByteMap.entry("minecraft:coarse_dirt", (byte) 8),
			Object2ByteMap.entry("minecraft:cobblestone", (byte) 9),
			Object2ByteMap.entry("minecraft:bedrock", (byte) 10),
			Object2ByteMap.entry("minecraft:oak_leaves", (byte) 11),
			Object2ByteMap.entry("minecraft:gray_wool", (byte) 12),
			Object2ByteMap.entry("minecraft:double_stone_slab", (byte) 13),
			Object2ByteMap.entry("minecraft:mossy_cobblestone", (byte) 14),
			Object2ByteMap.entry("minecraft:clay", (byte) 15),
			Object2ByteMap.entry("minecraft:stone_bricks", (byte) 16),
			Object2ByteMap.entry("minecraft:mossy_stone_bricks", (byte) 17),
			Object2ByteMap.entry("minecraft:chiseled_stone_bricks", (byte) 18),
			Object2ByteMap.entry("minecraft:gray_terracotta", (byte) 19),
			Object2ByteMap.entry("minecraft:cyan_terracotta", (byte) 20),
			Object2ByteMap.entry("minecraft:black_terracotta", (byte) 21)
	);
	/**
	 * Block data for dungeon rooms. See {@link de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU} for format details and how it's generated.
	 * All access to this map must check {@link #isRoomsLoaded()} to prevent concurrent modification.
	 */
	@SuppressWarnings("JavadocReference")
	protected static final Map<String, Map<String, Map<String, int[]>>> ROOMS_DATA = new ConcurrentHashMap<>();
	private static final Map<String, RoomInfo> ROOMS_INFO = new ConcurrentHashMap<>();
	private static final Map<String, List<RoomWaypoint>> ROOMS_WAYPOINTS = new ConcurrentHashMap<>();

	/**
	 * Rooms in the current dungeon map.
	 */
	private static final Map<Vector2ic, Room> rooms = new HashMap<>();

	/**
	 * The map of dungeon room names to custom waypoints relative to the room.
	 */
	private static final Table<String, BlockPos, SecretWaypoint> customWaypoints = HashBasedTable.create();
	private static @Nullable CompletableFuture<Void> roomsLoaded;
	/**
	 * The map position of the top left corner of the entrance room.
	 */
	private static @Nullable Vector2ic mapEntrancePos;
	/**
	 * The size of a room on the map.
	 */
	private static int mapRoomSize;
	/**
	 * The physical position of the northwest corner of the entrance room.
	 */

	private static @Nullable Vector2ic physicalEntrancePos;
	private static @Nullable Room currentRoom;
	private static DungeonBoss boss = DungeonBoss.NONE;
	private static @Nullable AABB bloodRushDoorBox;
	private static boolean bloodOpened;
	private static boolean hasKey;
	private static boolean runEnded;

	public static boolean isRoomsLoaded() {
		return roomsLoaded != null && roomsLoaded.isDone();
	}

	public static Stream<Room> getRoomsStream() {
		return rooms.values().stream();
	}

	public static @Nullable RoomInfo getRoomMetadata(String room) {
		return ROOMS_INFO.get(room);
	}

	public static @Nullable List<RoomWaypoint> getRoomWaypoints(String room) {
		return ROOMS_WAYPOINTS.get(room);
	}

	/**
	 * @see #customWaypoints
	 */
	public static Map<BlockPos, SecretWaypoint> getCustomWaypoints(String room) {
		return customWaypoints.row(room);
	}

	/**
	 * @see #customWaypoints
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static @Nullable SecretWaypoint addCustomWaypoint(String room, SecretWaypoint waypoint) {
		return customWaypoints.put(room, waypoint.pos, waypoint);
	}

	/**
	 * @see #customWaypoints
	 */
	public static void addCustomWaypoints(String room, Collection<SecretWaypoint> waypoints) {
		for (SecretWaypoint waypoint : waypoints) {
			addCustomWaypoint(room, waypoint);
		}
	}

	/**
	 * @see #customWaypoints
	 */
	public static @Nullable SecretWaypoint removeCustomWaypoint(String room, BlockPos pos) {
		return customWaypoints.remove(room, pos);
	}

	public static @Nullable Vector2ic getMapEntrancePos() {
		return mapEntrancePos;
	}

	public static int getMapRoomSize() {
		return mapRoomSize;
	}

	public static @Nullable Vector2ic getPhysicalEntrancePos() {
		return physicalEntrancePos;
	}

	/**
	 * not null if {@link #isCurrentRoomMatched()}
	 */
	public static @Nullable Room getCurrentRoom() {
		return currentRoom;
	}

	public static DungeonBoss getBoss() {
		return boss;
	}

	public static boolean isInBoss() {
		return boss.isInBoss();
	}

	/**
	 * Loads the dungeon secrets asynchronously from {@code /assets/skyblocker/dungeons}.
	 * Use {@link #isRoomsLoaded()} to check for completion of loading.
	 */
	@Init
	public static void init() {
		// Execute with MinecraftClient as executor since we need to wait for MinecraftClient#resourceManager to be set
		CompletableFuture.runAsync(DungeonManager::load, CLIENT).exceptionally(e -> {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets", e);
			return null;
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(DungeonManager::saveCustomWaypoints);
		Scheduler.INSTANCE.scheduleCyclic(DungeonManager::update, 5);
		Scheduler.INSTANCE.scheduleCyclic(DungeonManager::updateAllRoomCheckmarks, 20);
		WorldRenderExtractionCallback.EVENT.register(DungeonManager::extractRendering);
		ClientReceiveMessageEvents.ALLOW_GAME.register(DungeonManager::onChatMessage);
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onUseBlock(world, hitResult));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("secrets")
				.then(literal("markAsFound").then(markSecretsCommand(true)))
				.then(literal("markAsMissing").then(markSecretsCommand(false)).then(markAllSecretsAsMissingCommand()))
				.then(literal("getRelativePos").executes(DungeonManager::getRelativePos))
				.then(literal("getRelativeTargetPos").executes(DungeonManager::getRelativeTargetPos))
				.then(literal("addWaypoint").then(addCustomWaypointCommand(false, registryAccess)))
				.then(literal("addWaypointRelatively").then(addCustomWaypointCommand(true, registryAccess)))
				.then(literal("removeWaypoint").then(removeCustomWaypointCommand(false)))
				.then(literal("removeWaypointRelatively").then(removeCustomWaypointCommand(true)))
		))));
		if (Debug.debugEnabled()) {
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("secrets")
					.then(literal("matchAgainst").then(matchAgainstCommand()))
					.then(literal("clearSubProcesses").executes(context -> {
						if (currentRoom != null) {
							currentRoom.tickables.clear();
							currentRoom.renderables.clear();
							context.getSource().sendFeedback(Constants.PREFIX.get().append("§rCleared sub processes in the current room."));
						} else {
							context.getSource().sendError(Constants.PREFIX.get().append("§cCurrent room is null."));
						}
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("getCheckmarkColour").executes(context -> {
						MapItemSavedData state = getMapState(context.getSource().getClient());

						if (currentRoom != null && state != null) {
							int checkmarkColour = getRoomCheckmarkColour(state, currentRoom);
							String result = switch (checkmarkColour) {
								case DungeonMapUtils.GREEN_COLOR -> "Green";
								case DungeonMapUtils.WHITE_COLOR -> "White";
								case DungeonMapUtils.RED_COLOR -> "Red";
								default -> "Unknown";
							};

							context.getSource().sendFeedback(Constants.PREFIX.get().append("§rCheckmark colour: " + result));
						} else {
							context.getSource().sendError(Constants.PREFIX.get().append("§cCurrent room or map state is null."));
						}
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("clearRooms").executes(context -> {
						// Sometimes, you just need to start again from a clean slate.
						rooms.clear();
						context.getSource().sendFeedback(Constants.PREFIX.get().append("Cleared all rooms!"));
						return Command.SINGLE_SUCCESS;
					}))
			))));
		}
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> reset()));
	}

	private static void load() {
		long startTime = System.currentTimeMillis();
		List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
		for (Map.Entry<Identifier, Resource> resourceEntry : CLIENT.getResourceManager().listResources(DUNGEONS_PATH, id -> id.getPath().endsWith(".skeleton")).entrySet()) {
			checkResourceSource(resourceEntry.getKey(), resourceEntry.getValue());
			String[] path = resourceEntry.getKey().getPath().split("/");
			if (path.length != 4) {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon room data, invalid resource identifier {}", resourceEntry.getKey());
				break;
			}
			String dungeon = path[1];
			String roomShape = path[2];
			String room = path[3].substring(0, path[3].length() - ".skeleton".length());
			ROOMS_DATA.computeIfAbsent(dungeon, dungeonKey -> new ConcurrentHashMap<>());
			ROOMS_DATA.get(dungeon).computeIfAbsent(roomShape, roomShapeKey -> new ConcurrentHashMap<>());
			dungeonFutures.add(CompletableFuture.supplyAsync(() -> readRoom(resourceEntry.getValue()), Executors.newVirtualThreadPerTaskExecutor()).thenAcceptAsync(blocks -> {
				Map<String, int[]> roomsMap = ROOMS_DATA.get(dungeon).get(roomShape);
				roomsMap.put(room, blocks);
				LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon room skeleton - dungeon={}, shape={}, room={}", dungeon, roomShape, room);
			}, Executors.newVirtualThreadPerTaskExecutor()).exceptionally(e -> {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon room skeleton - dungeon={}, shape={}, room={}", dungeon, roomShape, room, e);
				return null;
			}));
		}

		for (Map.Entry<Identifier, Resource> resourceEntry : CLIENT.getResourceManager().listResources(DUNGEONS_PATH, id -> id.getPath().endsWith(".json")).entrySet()) {
			checkResourceSource(resourceEntry.getKey(), resourceEntry.getValue());
			String[] path = resourceEntry.getKey().getPath().split("/");
			if (path.length != 4) continue;
			String dungeon = path[1];
			String roomShape = path[2];
			String room = path[3].substring(0, path[3].length() - ".json".length());
			dungeonFutures.add(CompletableFuture.runAsync(() -> {
				try (BufferedReader roomJsonReader = CLIENT.getResourceManager().openAsReader(resourceEntry.getKey())) {
					RoomData roomData = RoomData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(roomJsonReader)).getOrThrow();
					ROOMS_WAYPOINTS.put(room, roomData.secrets);
					ROOMS_INFO.put(room, roomData.info);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon room secrets - dungeon={}, shape={}, room={}", dungeon, roomShape, room);
			}, Executors.newVirtualThreadPerTaskExecutor()).exceptionally(e -> {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load room secrets - dungeon={}, shape={}, room={}", dungeon, roomShape, room, e);
				return null;
			}));
		}

		dungeonFutures.add(CompletableFuture.runAsync(() -> {
			try (BufferedReader customWaypointsReader = Files.newBufferedReader(CUSTOM_WAYPOINTS_DIR)) {
				SkyblockerMod.GSON.fromJson(customWaypointsReader, JsonObject.class).asMap().forEach((room, waypointsJson) ->
						addCustomWaypoints(room, SecretWaypoint.LIST_CODEC.parse(JsonOps.INSTANCE, waypointsJson).resultOrPartial(LOGGER::error).orElseGet(ArrayList::new))
				);
				LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded custom dungeon secret waypoints");
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load custom dungeon secret waypoints", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor()));
		roomsLoaded = CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("[Skyblocker Dungeon Secrets] Loaded dungeon secrets for {} dungeon(s), {} room shapes, {} rooms, and {} custom secret waypoints total in {} ms", ROOMS_DATA.size(), ROOMS_DATA.values().stream().mapToInt(Map::size).sum(), ROOMS_DATA.values().stream().map(Map::values).flatMap(Collection::stream).mapToInt(Map::size).sum(), customWaypoints.size(), System.currentTimeMillis() - startTime)).exceptionally(e -> {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets", e);
			return null;
		});
		LOGGER.info("[Skyblocker Dungeon Secrets] Started loading dungeon secrets in (blocked main thread for) {} ms", System.currentTimeMillis() - startTime);
	}

	private static void saveCustomWaypoints(Minecraft client) {
		try (BufferedWriter writer = Files.newBufferedWriter(CUSTOM_WAYPOINTS_DIR)) {
			JsonObject customWaypointsJson = new JsonObject();
			customWaypoints.rowMap().forEach((room, waypoints) ->
					customWaypointsJson.add(room, SecretWaypoint.LIST_CODEC.encodeStart(JsonOps.INSTANCE, new ArrayList<>(waypoints.values())).resultOrPartial(LOGGER::error).orElseGet(JsonArray::new))
			);
			SkyblockerMod.GSON.toJson(customWaypointsJson, writer);
			LOGGER.info("[Skyblocker Dungeon Secrets] Saved custom dungeon secret waypoints");
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to save custom dungeon secret waypoints", e);
		}
	}

	private static int[] readRoom(Resource resource) throws RuntimeException {
		try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(resource.open()))) {
			return (int[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, Integer> markSecretsCommand(boolean found) {
		return argument("secretIndex", IntegerArgumentType.integer()).suggests((provider, builder) -> {
			if (isCurrentRoomMatched()) {
				//noinspection DataFlowIssue - checked above
				IntStream.rangeClosed(1, currentRoom.getSecretCount()).forEach(builder::suggest);
			}
			return builder.buildFuture();
		}).executes(context -> {
			int secretIndex = IntegerArgumentType.getInteger(context, "secretIndex");
			if (markSecrets(secretIndex, found)) {
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable(found ? "skyblocker.dungeons.secrets.markSecretFound" : "skyblocker.dungeons.secrets.markSecretMissing", secretIndex)));
			} else {
				context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable(found ? "skyblocker.dungeons.secrets.markSecretFoundUnable" : "skyblocker.dungeons.secrets.markSecretMissingUnable", secretIndex)));
			}
			return Command.SINGLE_SUCCESS;
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> markAllSecretsAsMissingCommand() {
		return literal("all").executes(context -> {
			if (isCurrentRoomMatched()) {
				//noinspection DataFlowIssue - checked above
				currentRoom.markAllSecrets(false);
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.markSecretsMissing")));
			} else {
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.markSecretsMissingUnable")));
			}

			return Command.SINGLE_SUCCESS;
		});
	}

	private static int getRelativePos(CommandContext<FabricClientCommandSource> context) {
		return getRelativePos(context.getSource(), context.getSource().getPlayer().blockPosition());
	}

	private static int getRelativeTargetPos(CommandContext<FabricClientCommandSource> context) {
		if (CLIENT.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
			return getRelativePos(context.getSource(), blockHitResult.getBlockPos());
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.noTarget")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int getRelativePos(FabricClientCommandSource source, BlockPos pos) {
		Room room = getRoomAtPhysical(pos);
		if (isRoomMatched(room)) {
			//noinspection DataFlowIssue - checked above
			BlockPos relativePos = currentRoom.actualToRelative(pos);
			//noinspection DataFlowIssue - checked above
			source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.posMessage", currentRoom.getName(), currentRoom.getDirection().getSerializedName(), relativePos.getX(), relativePos.getY(), relativePos.getZ())));
		} else {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, ClientPosArgument> addCustomWaypointCommand(boolean relative, CommandBuildContext registryAccess) {
		return argument("pos", ClientBlockPosArgumentType.blockPos())
				.then(argument("secretIndex", IntegerArgumentType.integer())
						.then(argument("category", SecretWaypoint.Category.CategoryArgumentType.category())
								.then(argument("name", ComponentArgument.textComponent(registryAccess)).executes(context -> {
									BlockPos pos = context.getArgument("pos", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
									return relative ? addCustomWaypointRelative(context, pos) : addCustomWaypoint(context, pos);
								}))
						)
				);
	}

	private static int addCustomWaypoint(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		Room room = getRoomAtPhysical(pos);
		if (isRoomMatched(room)) {
			room.addCustomWaypoint(context, room.actualToRelative(pos));
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int addCustomWaypointRelative(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		if (isCurrentRoomMatched()) {
			//noinspection DataFlowIssue - checked above
			currentRoom.addCustomWaypoint(context, pos);
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, ClientPosArgument> removeCustomWaypointCommand(boolean relative) {
		return argument("pos", ClientBlockPosArgumentType.blockPos())
				.executes(context -> {
					BlockPos pos = context.getArgument("pos", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
					return relative ? removeCustomWaypointRelative(context, pos) : removeCustomWaypoint(context, pos);
				});
	}

	private static int removeCustomWaypoint(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		Room room = getRoomAtPhysical(pos);
		if (isRoomMatched(room)) {
			room.removeCustomWaypoint(context, room.actualToRelative(pos));
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int removeCustomWaypointRelative(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		if (isCurrentRoomMatched()) {
			//noinspection DataFlowIssue - checked above
			currentRoom.removeCustomWaypoint(context, pos);
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, String> matchAgainstCommand() {
		return argument("room", StringArgumentType.string()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ROOMS_DATA.values().stream().map(Map::values).flatMap(Collection::stream).map(Map::keySet).flatMap(Collection::stream), builder)).then(argument("direction", Room.Direction.DirectionArgumentType.direction()).executes(context -> {
			if (!isClearingDungeon()) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cYou are not in a dungeon."));
				return Command.SINGLE_SUCCESS;
			}
			if (CLIENT.player == null || CLIENT.level == null) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get player or world."));
				return Command.SINGLE_SUCCESS;
			}
			ItemStack stack = CLIENT.player.getInventory().getNonEquipmentItems().get(8);
			if (!stack.is(Items.FILLED_MAP)) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map."));
				return Command.SINGLE_SUCCESS;
			}
			MapItemSavedData map = MapItem.getSavedData(stack.get(DataComponents.MAP_ID), CLIENT.level);
			if (map == null) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map state."));
				return Command.SINGLE_SUCCESS;
			}

			String roomName = StringArgumentType.getString(context, "room");
			Room.Direction direction = Room.Direction.DirectionArgumentType.getDirection(context, "direction");

			Room room = newDebugRoom(roomName, direction, CLIENT.player, map);
			if (room == null) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to find room with name " + roomName + "."));
				return Command.SINGLE_SUCCESS;
			}
			if (currentRoom != null) {
				currentRoom.addSubProcess(room);
				context.getSource().sendFeedback(Constants.PREFIX.get().append("§rMatching room " + roomName + " with direction " + direction + " against current room."));
			} else {
				context.getSource().sendError(Constants.PREFIX.get().append("§cCurrent room is null."));
			}

			return Command.SINGLE_SUCCESS;
		}));
	}

	private static @Nullable Room newDebugRoom(String roomName, Room.Direction direction, Player player, MapItemSavedData map) {
		Room room = null;
		int[] roomData;
		// we will clean this up one day (no we won't)
		if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.PUZZLE.shape).get(roomName)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.PUZZLE, DungeonMapUtils.getPhysicalRoomPos(player.position()), roomName, roomData, direction);
		} else if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.TRAP.shape).get(roomName)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.TRAP, DungeonMapUtils.getPhysicalRoomPos(player.position()), roomName, roomData, direction);
		} else if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.MINIBOSS.shape).get(roomName)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.MINIBOSS, DungeonMapUtils.getPhysicalRoomPos(player.position()), roomName, roomData, direction);
		} else if ((roomData = ROOMS_DATA.get("catacombs").values().stream().map(Map::entrySet).flatMap(Collection::stream).filter(entry -> entry.getKey().equals(roomName)).findAny().map(Map.Entry::getValue).orElse(null)) != null) {
			if (mapEntrancePos == null || physicalEntrancePos == null) return null;
			Vector2ic mapRoomPos = DungeonMapUtils.getMapRoomPos(map, mapEntrancePos, mapRoomSize);
			if (mapRoomPos == null) return null;
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.ROOM, DungeonMapUtils.getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, DungeonMapUtils.getRoomSegments(map, mapRoomPos, mapRoomSize, Room.Type.ROOM.color)), roomName, roomData, direction);
		}
		return room;
	}


	/**
	 * Gets the Mort NPC's location. This allows us to precisely locate the dungeon entrance
	 */
	public static @Nullable Vec3 getMortArmorStandPos() {
		if (CLIENT.level == null) return null;

		for (var entity : CLIENT.level.entitiesForRendering()) {
			if (entity instanceof ArmorStand armorStand) {
				Component name = armorStand.getCustomName();
				if (name != null && name.getString().contains("Mort")) {
					return armorStand.position();
				}
			}
		}
		return null;
	}

	/**
	 * Updates the dungeon. The general idea is similar to the Dungeon Rooms Mod.
	 * <p></p>
	 * When entering a new dungeon, this method:
	 * <ul>
	 *     <li> Gets the physical northwest corner position of the entrance room and saves it in {@link #physicalEntrancePos}. </li>
	 *     <li> Do nothing until the dungeon map exists. </li>
	 *     <li> Gets the upper left corner of entrance room on the map and saves it in {@link #mapEntrancePos}. </li>
	 *     <li> Gets the size of a room on the map in pixels and saves it in {@link #mapRoomSize}. </li>
	 *     <li> Creates a new {@link Room} with {@link Room.Type} {@link Room.Type.ENTRANCE ENTRANCE} and sets {@link #currentRoom}. </li>
	 * </ul>
	 * When processing an existing dungeon, this method:
	 * <ul>
	 *     <li> Calculates the physical northwest corner and upper left corner on the map of the room the player is currently in. </li>
	 *     <li> Gets the room type based on the map color. </li>
	 *     <li> If the room has not been created (when the physical northwest corner is not in {@link #rooms}):</li>
	 *     <ul>
	 *         <li> If the room type is {@link Room.Type.ROOM}, gets the northwest corner of all connected room segments with {@link DungeonMapUtils#getRoomSegments(MapItemSavedData, Vector2ic, int, byte)}.  (For example, a 1x2 room has two room segments.) </li>
	 *         <li> Create a new room. </li>
	 *     </ul>
	 *     <li> Sets {@link #currentRoom} to the current room, either created from the previous step or from {@link #rooms}. </li>
	 *     <li> Calls {@link Tickable#tick(Minecraft)} on {@link #currentRoom}. </li>
	 * </ul>
	 */
	@SuppressWarnings({"JavadocReference", "incomplete-switch"})
	private static void update() {
		if (!Utils.isInDungeons() || isInBoss() || runEnded) {
			return;
		}
		if (CLIENT.player == null || CLIENT.level == null) {
			return;
		}

		if (physicalEntrancePos == null) {
			//The check for the area should delay this until after the player's position has been set by the server (since the scoreboard should be sent after the player position)
			//this is necessary otherwise the default position of (0, 0) or whatever will mess up the entrance calculation which will break all sorts of things
			Vec3 mortPos = getMortArmorStandPos();
			if (mortPos == null) {
				LOGGER.warn("[Skyblocker Dungeon Secrets] Failed to find Mort armor stand, retrying...");
				return;
			} else {
				LOGGER.info("[Skyblocker Dungeon Secrets] Found Mort armor stand at position {}", mortPos);
			}
			physicalEntrancePos = DungeonMapUtils.getPhysicalRoomPos(mortPos);
			currentRoom = newRoom(Room.Type.ENTRANCE, physicalEntrancePos);
			DungeonEvents.DUNGEON_LOADED.invoker().onDungeonLoaded();
		}

		MapItemSavedData map = getMapState(CLIENT);
		if (map == null) {
			return;
		}
		if (mapEntrancePos == null || mapRoomSize == 0) {
			ObjectIntPair<Vector2ic> mapEntrancePosAndSize = DungeonMapUtils.getMapEntrancePosAndRoomSize(map);
			if (mapEntrancePosAndSize == null) {
				return;
			}
			mapEntrancePos = mapEntrancePosAndSize.left();
			mapRoomSize = mapEntrancePosAndSize.rightInt();
			LOGGER.info("[Skyblocker Dungeon Secrets] Started dungeon with map room size {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, CLIENT.player.position(), physicalEntrancePos);
		}

		getBloodRushDoorPos(map);

		Vector2ic physicalPos = DungeonMapUtils.getPhysicalRoomPos(CLIENT.player.position());
		Vector2ic mapPos = DungeonMapUtils.getMapPosFromPhysical(physicalEntrancePos, mapEntrancePos, mapRoomSize, physicalPos);
		Room room = rooms.get(physicalPos);
		if (room == null) {
			Room.Type type = DungeonMapUtils.getRoomType(map, mapPos);
			if (type == null || type == Room.Type.UNKNOWN) {
				return;
			}
			switch (type) {
				case ENTRANCE, PUZZLE, TRAP, MINIBOSS, FAIRY, BLOOD -> room = newRoom(type, physicalPos);
				case ROOM -> room = newRoom(type, DungeonMapUtils.getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, DungeonMapUtils.getRoomSegments(map, mapPos, mapRoomSize, type.color)));
			}
		}

		if (room != null && currentRoom != room) {
			currentRoom = room;
		}

		if (currentRoom == null) return;

		if (currentRoom.isMatched() && currentRoom.clearState != Room.ClearState.GREEN_CHECKED) {
			updateRoomCheckmark(currentRoom, map);
		}

		currentRoom.tick(CLIENT);
	}

	private static void updateAllRoomCheckmarks() {
		if (!Utils.isInDungeons() || isInBoss() || CLIENT.player == null || CLIENT.level == null) return;
		MapItemSavedData map = getMapState(CLIENT);
		if (map == null || mapEntrancePos == null) return;
		DungeonManager.getRoomsStream().filter(Room::isMatched).forEach(room -> updateRoomCheckmark(room, map));
	}

	// Calculate the checkmark colour and mark all secrets as found if the checkmark is green
	// We also wait for it being matched to ensure that we don't try to mark the room as completed if secret waypoints haven't yet loaded (since the room is still matching)
	// Mark the secret count as outdated to ensure we have an accurate count
	private static void updateRoomCheckmark(Room room, MapItemSavedData map) {
		if (room.clearState == Room.ClearState.GREEN_CHECKED) return;
		switch (getRoomCheckmarkColour(map, room)) {
			case DungeonMapUtils.GREEN_COLOR -> {
				room.clearState = Room.ClearState.GREEN_CHECKED;
				room.secretCountOutdated = true;
				room.markAllSecrets(true);
			}
			case DungeonMapUtils.WHITE_COLOR -> {
				if (room.clearState == Room.ClearState.WHITE_CHECKED) return;
				room.clearState = Room.ClearState.WHITE_CHECKED;
				room.secretCountOutdated = true;
			}
			case DungeonMapUtils.RED_COLOR -> room.clearState = Room.ClearState.FAILED;
			default -> room.clearState = Room.ClearState.UNCLEARED;
		}
	}

	/**
	 * Creates a new room with the given type and physical positions,
	 * adds the room to {@link #rooms}, and sets {@link #currentRoom} to the new room.
	 *
	 * @param type              the type of room to create
	 * @param physicalPositions the physical positions of the room
	 */
	private static @Nullable Room newRoom(Room.Type type, Vector2ic... physicalPositions) {
		try {
			Room newRoom = new Room(type, physicalPositions);
			for (Vector2ic physicalPos : physicalPositions) {
				rooms.put(physicalPos, newRoom);
			}
			return newRoom;
		} catch (IllegalArgumentException e) {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to create room", e);
		}
		return null;
	}

	/**
	 * Adds a room that was shared over the WebSocket.
	 */
	protected static void addRoomFromWs(Room room) {
		for (Vector2ic physicalPos : room.segments) {
			rooms.put(physicalPos, room);
		}
	}

	protected static boolean checkIfSegmentsExist(List<Vector2ic> segments) {
		return segments.stream().anyMatch(rooms::containsKey);
	}

	/**
	 * Extracts the rendering for secret waypoints in {@link #currentRoom} if {@link #shouldProcess()} and {@link #currentRoom} is not null.
	 */
	private static void extractRendering(PrimitiveCollector collector) {
		if (shouldProcess() && currentRoom != null) {
			currentRoom.extractRendering(collector);
		}

		if (bloodRushDoorBox != null && !bloodOpened && SkyblockerConfigManager.get().dungeons.doorHighlight.enableDoorHighlight) {
			float[] colorComponents = hasKey ? GREEN_COLOR_COMPONENTS : RED_COLOR_COMPONENTS;
			switch (SkyblockerConfigManager.get().dungeons.doorHighlight.doorHighlightType) {
				case HIGHLIGHT -> collector.submitFilledBox(bloodRushDoorBox, colorComponents, 0.5f, true);
				case OUTLINED_HIGHLIGHT -> {
					collector.submitFilledBox(bloodRushDoorBox, colorComponents, 0.5f, true);
					collector.submitOutlinedBox(bloodRushDoorBox, colorComponents, 5, true);
				}
				case OUTLINE -> collector.submitOutlinedBox(bloodRushDoorBox, colorComponents, 5, true);
			}
		}
	}

	/**
	 * Calls {@link Room#onChatMessage(String)} on {@link #currentRoom} if the message is an overlay message and {@link #isCurrentRoomMatched()} and processes key obtained messages.
	 * <p>Used to detect when all secrets in a room are found and detect when a wither or blood door is unlocked.
	 * To process key obtained messages, this method checks if door highlight is enabled and if the message matches a key obtained message.
	 */
	@SuppressWarnings("SameReturnValue")
	private static boolean onChatMessage(Component text, boolean overlay) {
		if (!shouldProcess()) return true;
		String message = text.getString();

		if (isCurrentRoomMatched()) {
			//noinspection DataFlowIssue - checked above
			currentRoom.onChatMessage(message);
		}

		// Process key found messages for door highlight
		if (SkyblockerConfigManager.get().dungeons.doorHighlight.enableDoorHighlight && !bloodOpened) {
			if (BLOOD_DOOR_OPENED.equals(message)) {
				bloodOpened = true;
			}

			if (KEY_FOUND.matcher(message).matches()) {
				hasKey = true;
			}

			if (WITHER_DOOR_OPENED.matcher(message).matches()) {
				hasKey = false;
			}
		}

		// Dungeon Events

		if (message.equals("§e[NPC] §bMort§f: You should find it useful if you get lost.")) {
			DungeonEvents.DUNGEON_STARTED.invoker().onDungeonStarted();
		}

		if (TEAM_SCORE_PATTERN.matcher(message).matches()) {
			DungeonEvents.DUNGEON_ENDED.invoker().onDungeonEnded();
			reset();
			// If the run ends during the clear, mark the run as ended so we don't spam the logs with finding Mort's armor stand.
			runEnded = true;
		}

		DungeonBoss newBoss = DungeonBoss.fromMessage(message);
		if (!isInBoss() && newBoss.isInBoss()) {
			reset();
			boss = newBoss;
		}

		return true;
	}

	/**
	 * Calls {@link Room#onUseBlock(Level, BlockHitResult)} on {@link #currentRoom} if {@link #isCurrentRoomMatched()}.
	 * Used to detect finding {@link SecretWaypoint.Category.CHEST} and {@link SecretWaypoint.Category.WITHER} secrets, as well as for hiding {@link SecretWaypoint.Category.LEVER} waypoints.
	 *
	 * @return {@link InteractionResult#PASS}
	 */
	@SuppressWarnings("JavadocReference")
	private static InteractionResult onUseBlock(Level world, BlockHitResult hitResult) {
		if (isCurrentRoomMatched()) {
			//noinspection DataFlowIssue - checked above
			currentRoom.onUseBlock(world, hitResult.getBlockPos());
		}
		return InteractionResult.PASS;
	}

	public static void onChestOpened(BlockPos pos) {
		if (isCurrentRoomMatched()) {
			currentRoom.onChestOpened(pos);
		}
	}

	/**
	 * Calls {@link Room#onItemPickup(ItemEntity, LivingEntity)} on the room the {@code collector} is in if that room {@link #isRoomMatched(Room)}.
	 * Used to detect finding {@link SecretWaypoint.Category.ITEM} secrets.
	 * If the collector is the player, {@link #currentRoom} is used as an optimization.
	 */
	@SuppressWarnings("JavadocReference")
	public static void onItemPickup(ItemEntity itemEntity) {
		Room room = getRoomAtPhysical(itemEntity.position());
		if (isRoomMatched(room)) {
			room.onItemPickup(itemEntity);
		}
	}

	/**
	 * Calls {@link Room#onBatRemoved(Bat)} on the room the {@code bat} is in if that room {@link #isRoomMatched(Room)}.
	 * Used to detect finding {@link SecretWaypoint.Category.BAT} secrets.
	 */
	@SuppressWarnings("JavadocReference")
	public static void onBatRemoved(AmbientCreature bat) {
		Room room = getRoomAtPhysical(bat.position());
		if (isRoomMatched(room)) {
			room.onBatRemoved(bat);
		}
	}

	public static boolean markSecrets(int secretIndex, boolean found) {
		if (isCurrentRoomMatched()) {
			//noinspection DataFlowIssue - checked above
			return currentRoom.markSecrets(secretIndex, found);
		}
		return false;
	}

	/**
	 * Gets the room at the given physical position.
	 *
	 * @param pos the physical position
	 * @return the room at the given physical position, or null if there is no room at the given physical position
	 * @see #rooms
	 * @see DungeonMapUtils#getPhysicalRoomPos(Vec3)
	 */
	private static @Nullable Room getRoomAtPhysical(Vec3 pos) {
		return rooms.get(DungeonMapUtils.getPhysicalRoomPos(pos));
	}

	/**
	 * Gets the room at the given physical position.
	 *
	 * @param pos the physical position
	 * @return the room at the given physical position, or null if there is no room at the given physical position
	 * @see #rooms
	 * @see DungeonMapUtils#getPhysicalRoomPos(Vec3i)
	 */
	private static @Nullable Room getRoomAtPhysical(Vec3i pos) {
		return rooms.get(DungeonMapUtils.getPhysicalRoomPos(pos));
	}

	/**
	 * Get the state of the map in the user's 9th slot.
	 */
	private static @Nullable MapItemSavedData getMapState(Minecraft client) {
		if (client.player == null) return null;
		return MapItem.getSavedData(DungeonMap.getMapIdComponent(client.player.getInventory().getNonEquipmentItems().get(8)), client.level);
	}

	/**
	 * @return {@code true} if the player is in the main clearing phase of a dungeon.
	 */
	public static boolean isClearingDungeon() {
		return physicalEntrancePos != null && mapEntrancePos != null && mapRoomSize != 0;
	}

	/**
	 * Calls {@link #isRoomMatched(Room)} on {@link #currentRoom}.
	 *
	 * @return {@code true} if {@link #currentRoom} is not null and {@link #isRoomMatched(Room)}
	 */
	public static boolean isCurrentRoomMatched() {
		return isRoomMatched(currentRoom);
	}

	/**
	 * Calls {@link #shouldProcess()} and {@link Room#isMatched()} on the given room.
	 *
	 * @param room the room to check
	 * @return {@code true} if {@link #shouldProcess()}, the given room is not null, and {@link Room#isMatched()} on the given room
	 */
	@Contract("null -> false")
	private static boolean isRoomMatched(@Nullable Room room) {
		return shouldProcess() && room != null && room.isMatched();
	}

	/**
	 * Checks if the player is in a dungeon.
	 *
	 * @return whether room matching and dungeon secrets should be processed
	 */
	private static boolean shouldProcess() {
		return Utils.isInDungeons();
	}

	/**
	 * Resets fields when leaving a dungeon or entering boss.
	 */
	private static void reset() {
		mapEntrancePos = null;
		mapRoomSize = 0;
		physicalEntrancePos = null;
		rooms.clear();
		currentRoom = null;
		boss = DungeonBoss.NONE;
		bloodRushDoorBox = null;
		bloodOpened = false;
		hasKey = false;
		runEnded = false;
	}

	/**
	 * Determines where the current door of interest is
	 *
	 * @implNote Relies on the minimap to check for doors
	 */
	private static void getBloodRushDoorPos(MapItemSavedData map) {
		if (mapEntrancePos == null || physicalEntrancePos == null || mapRoomSize == 0) {
			LOGGER.error("[Skyblocker Dungeon Secrets] Dungeon map info missing with map entrance pos {} and map room size {}", mapEntrancePos, mapRoomSize);
			return;
		}

		Vector2i nWMostRoom = getMapPosForNWMostRoom(mapEntrancePos, mapRoomSize);

		for (int x = nWMostRoom.x + mapRoomSize / 2; x < 128; x += mapRoomSize + 4) {
			for (int y = nWMostRoom.y + mapRoomSize; y < 128; y += mapRoomSize + 4) {
				byte color = getColor(map, x, y);

				// 119 is the black found on wither doors on the map, 18 is the blood door red
				if (color == 119 || color == 18) {
					Vector2ic doorPos = getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, new Vector2i(x - mapRoomSize / 2, y - mapRoomSize));
					bloodRushDoorBox = new AABB(doorPos.x() + 14, 69, doorPos.y() + 30, doorPos.x() + 17, 73, doorPos.y() + 33);

					return;
				}
			}
		}

		for (int x = nWMostRoom.x + mapRoomSize; x < 128; x += mapRoomSize + 4) {
			for (int y = nWMostRoom.y + mapRoomSize / 2; y < 128; y += mapRoomSize + 4) {
				byte color = getColor(map, x, y);

				if (color == 119 || color == 18) {
					Vector2ic doorPos = getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, new Vector2i(x - mapRoomSize, y - mapRoomSize / 2));
					bloodRushDoorBox = new AABB(doorPos.x() + 30, 69, doorPos.y() + 14, doorPos.x() + 33, 73, doorPos.y() + 17);

					return;
				}
			}
		}
	}

	/**
	 * Returns the colour of a room's checkmark on the map. To find a room's checkmark: For each segment, we start from the top left corner of the segment,
	 * go to the middle, then iterate downwards, and we should find the checkmark about a pixel or two down from there if the segment has the checkmark.
	 */
	private static int getRoomCheckmarkColour(MapItemSavedData mapState, Room room) {
		if (physicalEntrancePos == null || mapEntrancePos == null) return -1;
		int halfRoomSize = mapRoomSize / 2;

		//Check each segment of the room for the checkmark as each "block" of a room on the map is a separate segment, and we don't know which one has the checkmark
		//or more specifically which one is first the westernmost and second the northernmost (in the case of 2x2s).
		for (Vector2ic segmentPhysicalPos : room.segments) {
			Vector2ic topLeftCorner = DungeonMapUtils.getMapPosFromPhysical(physicalEntrancePos, mapEntrancePos, mapRoomSize, segmentPhysicalPos);
			Vector2ic middle = topLeftCorner.add(halfRoomSize, halfRoomSize, new Vector2i());

			//In this case, the offset is the number of units offset from the Y value of the middle of the segment
			for (int offset = 0; offset < halfRoomSize; offset++) {
				int colour = DungeonMapUtils.getColor(mapState, new Vector2i(middle.x(), middle.y() + offset));

				//Return if we found the colour of the checkmark
				if (colour == DungeonMapUtils.WHITE_COLOR || colour == DungeonMapUtils.GREEN_COLOR || colour == DungeonMapUtils.RED_COLOR) return colour;
			}
		}

		return -1;
	}

	@VisibleForTesting
	public static int getLoadedRoomCount() {
		return ROOMS_INFO.size();
	}

	// The dungeon .skeleton and room .json assets are critical to these features working correctly and they must remain in an unmodified form to prevent unexpected behaviour,
	// if you need to change something in them for some reason please contact us and provide us with your use case so we can work something out.
	// Note that modifying these resources was never supported to begin with.
	private static void checkResourceSource(Identifier id, Resource resource) {
		Utils.checkForIllegalResourceModification(id, resource, "[Skyblocker] Modifying the resource {} with resource packs is NOT SUPPORTED! This asset is important and must remain unmodified. Contact the Skyblocker devs for more info, ****THE GAME WILL NOW BE LOST****. Evil Resource Pack: {}");
	}

	public record RoomInfo(String name) {
		public static final Codec<RoomInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(RoomInfo::name)
		).apply(instance, RoomInfo::new));
	}

	public record RoomWaypoint(String secretName, SecretWaypoint.Category category, int x, int y, int z) {
		private static final Codec<RoomWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("secretName").forGetter(RoomWaypoint::secretName),
				SecretWaypoint.Category.CODEC.fieldOf("category").forGetter(RoomWaypoint::category),
				Codec.INT.fieldOf("x").forGetter(RoomWaypoint::x),
				Codec.INT.fieldOf("y").forGetter(RoomWaypoint::y),
				Codec.INT.fieldOf("z").forGetter(RoomWaypoint::z)
		).apply(instance, RoomWaypoint::new));
		public static final Codec<List<RoomWaypoint>> LIST_CODEC = CODEC.listOf();
	}

	public record RoomData(RoomInfo info, List<RoomWaypoint> secrets) {
		public static final Codec<RoomData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RoomInfo.CODEC.fieldOf("info").forGetter(RoomData::info),
				RoomWaypoint.LIST_CODEC.fieldOf("secrets").forGetter(RoomData::secrets)
		).apply(instance, RoomData::new));
	}
}
