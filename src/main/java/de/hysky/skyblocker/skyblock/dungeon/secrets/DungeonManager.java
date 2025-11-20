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
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.joml.Vector2i;
import org.joml.Vector2ic;
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
import it.unimi.dsi.fastutil.objects.Object2ByteMaps;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.Registry;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class DungeonManager {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	protected static final Logger LOGGER = LoggerFactory.getLogger(DungeonManager.class);
	@VisibleForTesting
	public static final String DUNGEONS_PATH = "dungeons";
	private static Path CUSTOM_WAYPOINTS_DIR;
	private static final Pattern KEY_FOUND = Pattern.compile("^RIGHT CLICK on (?:the BLOOD DOOR|a WITHER door) to open it. This key can only be used to open 1 door!$");
	private static final Pattern WITHER_DOOR_OPENED = Pattern.compile("^\\w+ opened a WITHER door!$");
	private static final String BLOOD_DOOR_OPENED = "The BLOOD DOOR has been opened!";
	protected static final float[] RED_COLOR_COMPONENTS = {1, 0, 0};
	protected static final float[] GREEN_COLOR_COMPONENTS = {0, 1, 0};
	/**
	 * Maps the block identifier string to a custom numeric block id used in dungeon rooms data.
	 *
	 * @implNote Not using {@link Registry#getId(Object) Registry#getId(Block)} and {@link Blocks Blocks} since this is also used by {@link de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU}, which runs outside of Minecraft.
	 */
	@SuppressWarnings("JavadocReference")
	protected static final Object2ByteMap<String> NUMERIC_ID = Object2ByteMaps.unmodifiable(new Object2ByteOpenHashMap<>(Map.ofEntries(
			Map.entry("minecraft:stone", (byte) 1),
			Map.entry("minecraft:diorite", (byte) 2),
			Map.entry("minecraft:polished_diorite", (byte) 3),
			Map.entry("minecraft:andesite", (byte) 4),
			Map.entry("minecraft:polished_andesite", (byte) 5),
			Map.entry("minecraft:grass_block", (byte) 6),
			Map.entry("minecraft:dirt", (byte) 7),
			Map.entry("minecraft:coarse_dirt", (byte) 8),
			Map.entry("minecraft:cobblestone", (byte) 9),
			Map.entry("minecraft:bedrock", (byte) 10),
			Map.entry("minecraft:oak_leaves", (byte) 11),
			Map.entry("minecraft:gray_wool", (byte) 12),
			Map.entry("minecraft:double_stone_slab", (byte) 13),
			Map.entry("minecraft:mossy_cobblestone", (byte) 14),
			Map.entry("minecraft:clay", (byte) 15),
			Map.entry("minecraft:stone_bricks", (byte) 16),
			Map.entry("minecraft:mossy_stone_bricks", (byte) 17),
			Map.entry("minecraft:chiseled_stone_bricks", (byte) 18),
			Map.entry("minecraft:gray_terracotta", (byte) 19),
			Map.entry("minecraft:cyan_terracotta", (byte) 20),
			Map.entry("minecraft:black_terracotta", (byte) 21)
	)));
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
	@Nullable
	private static CompletableFuture<Void> roomsLoaded;
	/**
	 * The map position of the top left corner of the entrance room.
	 */
	@Nullable
	private static Vector2ic mapEntrancePos;
	/**
	 * The size of a room on the map.
	 */
	private static int mapRoomSize;
	/**
	 * The physical position of the northwest corner of the entrance room.
	 */
	@Nullable
	private static Vector2ic physicalEntrancePos;
	private static Room currentRoom;
	@NotNull
	private static DungeonBoss boss = DungeonBoss.NONE;
	@Nullable
	private static Box bloodRushDoorBox;
	private static boolean bloodOpened;
	private static boolean hasKey;

	public static boolean isRoomsLoaded() {
		return roomsLoaded != null && roomsLoaded.isDone();
	}

	public static Stream<Room> getRoomsStream() {
		return rooms.values().stream();
	}

	@Nullable
	public static RoomInfo getRoomMetadata(String room) {
		return ROOMS_INFO.get(room);
	}

	@Nullable
	public static List<RoomWaypoint> getRoomWaypoints(String room) {
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
	public static SecretWaypoint addCustomWaypoint(String room, SecretWaypoint waypoint) {
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
	@Nullable
	public static SecretWaypoint removeCustomWaypoint(String room, BlockPos pos) {
		return customWaypoints.remove(room, pos);
	}

	@Nullable
	public static Vector2ic getMapEntrancePos() {
		return mapEntrancePos;
	}

	public static int getMapRoomSize() {
		return mapRoomSize;
	}

	@Nullable
	public static Vector2ic getPhysicalEntrancePos() {
		return physicalEntrancePos;
	}

	/**
	 * not null if {@link #isCurrentRoomMatched()}
	 */
	public static Room getCurrentRoom() {
		return currentRoom;
	}

	@NotNull
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
		CUSTOM_WAYPOINTS_DIR = SkyblockerMod.CONFIG_DIR.resolve("custom_secret_waypoints.json");

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
						MapState state = getMapState(context.getSource().getClient());

						if (currentRoom != null && state != null) {
							int checkmarkColour = getRoomCheckmarkColour(context.getSource().getClient(), state, currentRoom);
							String result = switch ((Integer) checkmarkColour) {
								case Integer i when i == DungeonMapUtils.WHITE_COLOR -> "White";
								case Integer i when i == DungeonMapUtils.GREEN_COLOR -> "Green";
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
		for (Map.Entry<Identifier, Resource> resourceEntry : CLIENT.getResourceManager().findResources(DUNGEONS_PATH, id -> id.getPath().endsWith(".skeleton")).entrySet()) {
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
			dungeonFutures.add(CompletableFuture.supplyAsync(() -> readRoom(resourceEntry.getValue())).thenAcceptAsync(blocks -> {
				Map<String, int[]> roomsMap = ROOMS_DATA.get(dungeon).get(roomShape);
				roomsMap.put(room, blocks);
				LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon room skeleton - dungeon={}, shape={}, room={}", dungeon, roomShape, room);
			}).exceptionally(e -> {
				LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon room skeleton - dungeon={}, shape={}, room={}", dungeon, roomShape, room, e);
				return null;
			}));
		}

		for (Map.Entry<Identifier, Resource> resourceEntry : CLIENT.getResourceManager().findResources(DUNGEONS_PATH, id -> id.getPath().endsWith(".json")).entrySet()) {
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
			}).exceptionally(e -> {
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
		}));
		roomsLoaded = CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("[Skyblocker Dungeon Secrets] Loaded dungeon secrets for {} dungeon(s), {} room shapes, {} rooms, and {} custom secret waypoints total in {} ms", ROOMS_DATA.size(), ROOMS_DATA.values().stream().mapToInt(Map::size).sum(), ROOMS_DATA.values().stream().map(Map::values).flatMap(Collection::stream).mapToInt(Map::size).sum(), customWaypoints.size(), System.currentTimeMillis() - startTime)).exceptionally(e -> {
			LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets", e);
			return null;
		});
		LOGGER.info("[Skyblocker Dungeon Secrets] Started loading dungeon secrets in (blocked main thread for) {} ms", System.currentTimeMillis() - startTime);
	}

	private static void saveCustomWaypoints(MinecraftClient client) {
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
		try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(resource.getInputStream()))) {
			return (int[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, Integer> markSecretsCommand(boolean found) {
		return argument("secretIndex", IntegerArgumentType.integer()).suggests((provider, builder) -> {
			if (isCurrentRoomMatched()) {
				IntStream.rangeClosed(1, currentRoom.getSecretCount()).forEach(builder::suggest);
			}
			return builder.buildFuture();
		}).executes(context -> {
			int secretIndex = IntegerArgumentType.getInteger(context, "secretIndex");
			if (markSecrets(secretIndex, found)) {
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable(found ? "skyblocker.dungeons.secrets.markSecretFound" : "skyblocker.dungeons.secrets.markSecretMissing", secretIndex)));
			} else {
				context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable(found ? "skyblocker.dungeons.secrets.markSecretFoundUnable" : "skyblocker.dungeons.secrets.markSecretMissingUnable", secretIndex)));
			}
			return Command.SINGLE_SUCCESS;
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> markAllSecretsAsMissingCommand() {
		return literal("all").executes(context -> {
			if (isCurrentRoomMatched()) {
				currentRoom.markAllSecrets(false);
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.markSecretsMissing")));
			} else {
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.markSecretsMissingUnable")));
			}

			return Command.SINGLE_SUCCESS;
		});
	}

	private static int getRelativePos(CommandContext<FabricClientCommandSource> context) {
		return getRelativePos(context.getSource(), context.getSource().getPlayer().getBlockPos());
	}

	private static int getRelativeTargetPos(CommandContext<FabricClientCommandSource> context) {
		if (CLIENT.crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
			return getRelativePos(context.getSource(), blockHitResult.getBlockPos());
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.noTarget")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int getRelativePos(FabricClientCommandSource source, BlockPos pos) {
		Room room = getRoomAtPhysical(pos);
		if (isRoomMatched(room)) {
			BlockPos relativePos = currentRoom.actualToRelative(pos);
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.posMessage", currentRoom.getName(), currentRoom.getDirection().asString(), relativePos.getX(), relativePos.getY(), relativePos.getZ())));
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, ClientPosArgument> addCustomWaypointCommand(boolean relative, CommandRegistryAccess registryAccess) {
		return argument("pos", ClientBlockPosArgumentType.blockPos())
				.then(argument("secretIndex", IntegerArgumentType.integer())
						.then(argument("category", SecretWaypoint.Category.CategoryArgumentType.category())
								.then(argument("name", TextArgumentType.text(registryAccess)).executes(context -> {
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
			context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int addCustomWaypointRelative(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		if (isCurrentRoomMatched()) {
			currentRoom.addCustomWaypoint(context, pos);
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")));
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
			context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int removeCustomWaypointRelative(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		if (isCurrentRoomMatched()) {
			currentRoom.removeCustomWaypoint(context, pos);
		} else {
			context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")));
		}
		return Command.SINGLE_SUCCESS;
	}

	private static RequiredArgumentBuilder<FabricClientCommandSource, String> matchAgainstCommand() {
		return argument("room", StringArgumentType.string()).suggests((context, builder) -> CommandSource.suggestMatching(ROOMS_DATA.values().stream().map(Map::values).flatMap(Collection::stream).map(Map::keySet).flatMap(Collection::stream), builder)).then(argument("direction", Room.Direction.DirectionArgumentType.direction()).executes(context -> {
			if (!isClearingDungeon()) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cYou are not in a dungeon."));
				return Command.SINGLE_SUCCESS;
			}
			if (CLIENT.player == null || CLIENT.world == null) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get player or world."));
				return Command.SINGLE_SUCCESS;
			}
			ItemStack stack = CLIENT.player.getInventory().getMainStacks().get(8);
			if (!stack.isOf(Items.FILLED_MAP)) {
				context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map."));
				return Command.SINGLE_SUCCESS;
			}
			MapState map = FilledMapItem.getMapState(stack.get(DataComponentTypes.MAP_ID), CLIENT.world);
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

	@Nullable
	private static Room newDebugRoom(String roomName, Room.Direction direction, PlayerEntity player, MapState map) {
		Room room = null;
		int[] roomData;
		// we will clean this up one day (no we won't)
		if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.PUZZLE.shape).get(roomName)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.PUZZLE, DungeonMapUtils.getPhysicalRoomPos(player.getEntityPos()), roomName, roomData, direction);
		} else if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.TRAP.shape).get(roomName)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.TRAP, DungeonMapUtils.getPhysicalRoomPos(player.getEntityPos()), roomName, roomData, direction);
		} else if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.MINIBOSS.shape).get(roomName)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.MINIBOSS, DungeonMapUtils.getPhysicalRoomPos(player.getEntityPos()), roomName, roomData, direction);
		} else if ((roomData = ROOMS_DATA.get("catacombs").values().stream().map(Map::entrySet).flatMap(Collection::stream).filter(entry -> entry.getKey().equals(roomName)).findAny().map(Map.Entry::getValue).orElse(null)) != null) {
			room = DebugRoom.ofSinglePossibleRoom(Room.Type.ROOM, DungeonMapUtils.getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, DungeonMapUtils.getRoomSegments(map, DungeonMapUtils.getMapRoomPos(map, mapEntrancePos, mapRoomSize), mapRoomSize, Room.Type.ROOM.color)), roomName, roomData, direction);
		}
		return room;
	}


	/**
	 * Gets the Mort NPC's location. This allows us to precisely locate the dungeon entrance
	 */
	public static Vec3d getMortArmorStandPos() {
		if (CLIENT.world == null) return null;

		for (var entity : CLIENT.world.getEntities()) {
			if (entity instanceof ArmorStandEntity armorStand) {
				Text name = armorStand.getCustomName();
				if (name != null && name.getString().contains("Mort")) {
					return armorStand.getEntityPos();
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
	 *         <li> If the room type is {@link Room.Type.ROOM}, gets the northwest corner of all connected room segments with {@link DungeonMapUtils#getRoomSegments(MapState, Vector2ic, int, byte)}.  (For example, a 1x2 room has two room segments.) </li>
	 *         <li> Create a new room. </li>
	 *     </ul>
	 *     <li> Sets {@link #currentRoom} to the current room, either created from the previous step or from {@link #rooms}. </li>
	 *     <li> Calls {@link Tickable#tick(MinecraftClient)} on {@link #currentRoom}. </li>
	 * </ul>
	 */
	@SuppressWarnings({"JavadocReference", "incomplete-switch"})
	private static void update() {
		if (!Utils.isInDungeons() || isInBoss()) {
			return;
		}
		if (CLIENT.player == null || CLIENT.world == null) {
			return;
		}

		if (physicalEntrancePos == null) {
			//The check for the area should delay this until after the player's position has been set by the server (since the scoreboard should be sent after the player position)
			//this is necessary otherwise the default position of (0, 0) or whatever will mess up the entrance calculation which will break all sorts of things
			Vec3d mortPos = getMortArmorStandPos();
			if (mortPos == null) {
				LOGGER.warn("[Skyblocker Dungeon Secrets] Failed to find Mort armor stand, retrying...");
				return;
			}
			physicalEntrancePos = DungeonMapUtils.getPhysicalRoomPos(mortPos);
			currentRoom = newRoom(Room.Type.ENTRANCE, physicalEntrancePos);
			DungeonEvents.DUNGEON_LOADED.invoker().onDungeonLoaded();
		}

		MapState map = getMapState(CLIENT);
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
			LOGGER.info("[Skyblocker Dungeon Secrets] Started dungeon with map room size {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, CLIENT.player.getEntityPos(), physicalEntrancePos);
		}

		getBloodRushDoorPos(map);

		Vector2ic physicalPos = DungeonMapUtils.getPhysicalRoomPos(CLIENT.player.getEntityPos());
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

		if (currentRoom.isMatched() && (!currentRoom.greenChecked || !currentRoom.whiteChecked)) {
			updateRoomCheckmark(currentRoom, map);
		}

		currentRoom.tick(CLIENT);
	}

	private static void updateAllRoomCheckmarks() {
		if (!Utils.isInDungeons() || isInBoss() || CLIENT.player == null || CLIENT.world == null) return;
		MapState map = getMapState(CLIENT);
		if (map == null || mapEntrancePos == null) return;
		DungeonManager.getRoomsStream().filter(Room::isMatched).forEach(room -> updateRoomCheckmark(room, map));
	}

	// Calculate the checkmark colour and mark all secrets as found if the checkmark is green
	// We also wait for it being matched to ensure that we don't try to mark the room as completed if secret waypoints haven't yet loaded (since the room is still matching)
	private static void updateRoomCheckmark(Room room, MapState map) {
		if (room.getType() == Room.Type.ENTRANCE || room.greenChecked && room.whiteChecked) return;
		if (!room.greenChecked && getRoomCheckmarkColour(CLIENT, map, room) == DungeonMapUtils.GREEN_COLOR) {
			room.greenChecked = true;
			room.whiteChecked = true;
			room.markAllSecrets(true);
		} else if (!room.whiteChecked && getRoomCheckmarkColour(CLIENT, map, room) == DungeonMapUtils.WHITE_COLOR) {
			room.whiteChecked = true;
		}
	}

	/**
	 * Creates a new room with the given type and physical positions,
	 * adds the room to {@link #rooms}, and sets {@link #currentRoom} to the new room.
	 *
	 * @param type              the type of room to create
	 * @param physicalPositions the physical positions of the room
	 */
	@Nullable
	private static Room newRoom(Room.Type type, Vector2ic... physicalPositions) {
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
	private static boolean onChatMessage(Text text, boolean overlay) {
		if (!shouldProcess()) {
			return true;
		}

		String message = text.getString();

		if (isCurrentRoomMatched()) {
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

		if (message.equals("§e[NPC] §bMort§f: You should find it useful if you get lost.")) {
			DungeonEvents.DUNGEON_STARTED.invoker().onDungeonStarted();
		}

		var newBoss = DungeonBoss.fromMessage(message);
		if (!isInBoss() && newBoss.isInBoss()) {
			reset();
			boss = newBoss;
		}

		return true;
	}

	/**
	 * Calls {@link Room#onUseBlock(World, BlockHitResult)} on {@link #currentRoom} if {@link #isCurrentRoomMatched()}.
	 * Used to detect finding {@link SecretWaypoint.Category.CHEST} and {@link SecretWaypoint.Category.WITHER} secrets.
	 *
	 * @return {@link ActionResult#PASS}
	 */
	@SuppressWarnings("JavadocReference")
	private static ActionResult onUseBlock(World world, BlockHitResult hitResult) {
		if (isCurrentRoomMatched()) {
			currentRoom.onUseBlock(world, hitResult.getBlockPos());
		}
		return ActionResult.PASS;
	}

	/**
	 * Calls {@link Room#onItemPickup(ItemEntity, LivingEntity)} on the room the {@code collector} is in if that room {@link #isRoomMatched(Room)}.
	 * Used to detect finding {@link SecretWaypoint.Category.ITEM} secrets.
	 * If the collector is the player, {@link #currentRoom} is used as an optimization.
	 */
	@SuppressWarnings("JavadocReference")
	public static void onItemPickup(ItemEntity itemEntity) {
		Room room = getRoomAtPhysical(itemEntity.getEntityPos());
		if (isRoomMatched(room)) {
			room.onItemPickup(itemEntity);
		}
	}

	/**
	 * Calls {@link Room#onBatRemoved(BatEntity)} on the room the {@code bat} is in if that room {@link #isRoomMatched(Room)}.
	 * Used to detect finding {@link SecretWaypoint.Category.BAT} secrets.
	 */
	@SuppressWarnings("JavadocReference")
	public static void onBatRemoved(AmbientEntity bat) {
		Room room = getRoomAtPhysical(bat.getEntityPos());
		if (isRoomMatched(room)) {
			room.onBatRemoved(bat);
		}
	}

	public static boolean markSecrets(int secretIndex, boolean found) {
		if (isCurrentRoomMatched()) {
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
	 * @see DungeonMapUtils#getPhysicalRoomPos(Vec3d)
	 */
	@Nullable
	private static Room getRoomAtPhysical(Vec3d pos) {
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
	@Nullable
	private static Room getRoomAtPhysical(Vec3i pos) {
		return rooms.get(DungeonMapUtils.getPhysicalRoomPos(pos));
	}

	/**
	 * Get the state of the map in the user's 9th slot.
	 */
	@Nullable
	private static MapState getMapState(MinecraftClient client) {
		if (client.player == null) return null;
		return FilledMapItem.getMapState(DungeonMap.getMapIdComponent(client.player.getInventory().getMainStacks().get(8)), client.world);
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
	}

	/**
	 * Determines where the current door of interest is
	 *
	 * @implNote Relies on the minimap to check for doors
	 */
	private static void getBloodRushDoorPos(@NotNull MapState map) {
		if (mapEntrancePos == null || mapRoomSize == 0) {
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
					bloodRushDoorBox = new Box(doorPos.x() + 14, 69, doorPos.y() + 30, doorPos.x() + 17, 73, doorPos.y() + 33);

					return;
				}
			}
		}

		for (int x = nWMostRoom.x + mapRoomSize; x < 128; x += mapRoomSize + 4) {
			for (int y = nWMostRoom.y + mapRoomSize / 2; y < 128; y += mapRoomSize + 4) {
				byte color = getColor(map, x, y);

				if (color == 119 || color == 18) {
					Vector2ic doorPos = getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, new Vector2i(x - mapRoomSize, y - mapRoomSize / 2));
					bloodRushDoorBox = new Box(doorPos.x() + 30, 69, doorPos.y() + 14, doorPos.x() + 33, 73, doorPos.y() + 17);

					return;
				}
			}
		}
	}

	/**
	 * Returns the colour of a room's checkmark on the map. To find a room's checkmark: For each segment, we start from the top left corner of the segment,
	 * go to the middle, then iterate downwards, and we should find the checkmark about a pixel or two down from there if the segment has the checkmark.
	 */
	private static int getRoomCheckmarkColour(MinecraftClient client, MapState mapState, Room room) {
		int halfRoomSize = mapRoomSize / 2;

		//Check each segment of the room for the checkmark as each "block" of a room on the map is a separate segment, and we don't know which one has the checkmark
		//or more specifically which one is first the western most and second the northern most (in the case of 2x2s).
		for (Vector2ic segmentPhysicalPos : room.segments) {
			Vector2ic topLeftCorner = DungeonMapUtils.getMapPosFromPhysical(physicalEntrancePos, mapEntrancePos, mapRoomSize, segmentPhysicalPos);
			Vector2ic middle = topLeftCorner.add(halfRoomSize, halfRoomSize, new Vector2i());

			//In this case, the offset is the number of units offset from the Y value of the middle of the segment
			for (int offset = 0; offset < halfRoomSize; offset++) {
				int colour = DungeonMapUtils.getColor(mapState, new Vector2i(middle.x(), middle.y() + offset));

				//Return if we found the colour of the checkmark
				if (colour == DungeonMapUtils.WHITE_COLOR || colour == DungeonMapUtils.GREEN_COLOR) return colour;
			}
		}

		return -1;
	}

	@VisibleForTesting
	public static int getLoadedRoomCount() {
		return ROOMS_INFO.size();
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
