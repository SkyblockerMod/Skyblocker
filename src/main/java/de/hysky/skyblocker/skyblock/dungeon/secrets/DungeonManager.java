package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMap;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.render.RenderHelper;
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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

import static de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class DungeonManager {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DungeonManager.class);
    private static final String DUNGEONS_PATH = "dungeons";
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
    protected static final HashMap<String, Map<String, Map<String, int[]>>> ROOMS_DATA = new HashMap<>();
    @NotNull
    private static final Map<Vector2ic, Room> rooms = new HashMap<>();
    private static final Map<String, JsonElement> roomsJson = new HashMap<>();
    private static final Map<String, JsonElement> waypointsJson = new HashMap<>();
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

    @SuppressWarnings("unused")
    public static JsonObject getRoomMetadata(String room) {
        JsonElement value = roomsJson.get(room);
        return value != null ? value.getAsJsonObject() : null;
    }

    public static JsonArray getRoomWaypoints(String room) {
        JsonElement value = waypointsJson.get(room);
        return value != null ? value.getAsJsonArray() : null;
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
    public static void init() {
        CUSTOM_WAYPOINTS_DIR = SkyblockerMod.CONFIG_DIR.resolve("custom_secret_waypoints.json");
        if (!SkyblockerConfigManager.get().dungeons.secretWaypoints.enableRoomMatching) {
            return;
        }
        // Execute with MinecraftClient as executor since we need to wait for MinecraftClient#resourceManager to be set
        CompletableFuture.runAsync(DungeonManager::load, MinecraftClient.getInstance()).exceptionally(e -> {
            LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets", e);
            return null;
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(DungeonManager::saveCustomWaypoints);
        Scheduler.INSTANCE.scheduleCyclic(DungeonManager::update, 5);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DungeonManager::render);
        ClientReceiveMessageEvents.GAME.register(DungeonManager::onChatMessage);
        ClientReceiveMessageEvents.GAME_CANCELED.register(DungeonManager::onChatMessage);
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
            ))));
        }
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> reset()));
    }

    private static void load() {
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> resourceEntry : MinecraftClient.getInstance().getResourceManager().findResources(DUNGEONS_PATH, id -> id.getPath().endsWith(".skeleton")).entrySet()) {
            String[] path = resourceEntry.getKey().getPath().split("/");
            if (path.length != 4) {
                LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets, invalid resource identifier {}", resourceEntry.getKey());
                break;
            }
            String dungeon = path[1];
            String roomShape = path[2];
            String room = path[3].substring(0, path[3].length() - ".skeleton".length());
            ROOMS_DATA.computeIfAbsent(dungeon, dungeonKey -> new HashMap<>());
            ROOMS_DATA.get(dungeon).computeIfAbsent(roomShape, roomShapeKey -> new HashMap<>());
            dungeonFutures.add(CompletableFuture.supplyAsync(() -> readRoom(resourceEntry.getValue())).thenAcceptAsync(rooms -> {
                Map<String, int[]> roomsMap = ROOMS_DATA.get(dungeon).get(roomShape);
                synchronized (roomsMap) {
                    roomsMap.put(room, rooms);
                }
                LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon secrets dungeon {} room shape {} room {}", dungeon, roomShape, room);
            }).exceptionally(e -> {
                LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secrets dungeon {} room shape {} room {}", dungeon, roomShape, room, e);
                return null;
            }));
        }
        dungeonFutures.add(CompletableFuture.runAsync(() -> {
            try (BufferedReader roomsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(Identifier.of(SkyblockerMod.NAMESPACE, "dungeons/dungeonrooms.json")); BufferedReader waypointsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(Identifier.of(SkyblockerMod.NAMESPACE, "dungeons/secretlocations.json"))) {
                loadJson(roomsReader, roomsJson);
                loadJson(waypointsReader, waypointsJson);
                LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded dungeon secret waypoints json");
            } catch (Exception e) {
                LOGGER.error("[Skyblocker Dungeon Secrets] Failed to load dungeon secret waypoints json", e);
            }
        }));
        dungeonFutures.add(CompletableFuture.runAsync(() -> {
            try (BufferedReader customWaypointsReader = Files.newBufferedReader(CUSTOM_WAYPOINTS_DIR)) {
                SkyblockerMod.GSON.fromJson(customWaypointsReader, JsonObject.class).asMap().forEach((room, waypointsJson) ->
                        addCustomWaypoints(room, SecretWaypoint.LIST_CODEC.parse(JsonOps.INSTANCE, waypointsJson).resultOrPartial(LOGGER::error).orElseGet(ArrayList::new))
                );
                LOGGER.debug("[Skyblocker Dungeon Secrets] Loaded custom dungeon secret waypoints");
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

    /**
     * Loads the json from the given {@link BufferedReader} into the given {@link Map}.
     *
     * @param reader the reader to read the json from
     * @param map    the map to load into
     */
    private static void loadJson(BufferedReader reader, Map<String, JsonElement> map) {
        SkyblockerMod.GSON.fromJson(reader, JsonObject.class).asMap().forEach((room, jsonElement) -> map.put(room.toLowerCase().replaceAll(" ", "-"), jsonElement));
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
        if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
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
            if (physicalEntrancePos == null || mapEntrancePos == null || mapRoomSize == 0) {
                context.getSource().sendError(Constants.PREFIX.get().append("§cYou are not in a dungeon."));
                return Command.SINGLE_SUCCESS;
            }
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get player or world."));
                return Command.SINGLE_SUCCESS;
            }
            ItemStack stack = client.player.getInventory().main.get(8);
            if (!stack.isOf(Items.FILLED_MAP)) {
                context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map."));
                return Command.SINGLE_SUCCESS;
            }
            MapState map = FilledMapItem.getMapState(stack.get(DataComponentTypes.MAP_ID), client.world);
            if (map == null) {
                context.getSource().sendError(Constants.PREFIX.get().append("§cFailed to get dungeon map state."));
                return Command.SINGLE_SUCCESS;
            }

            String roomName = StringArgumentType.getString(context, "room");
            Room.Direction direction = Room.Direction.DirectionArgumentType.getDirection(context, "direction");

            Room room = newDebugRoom(roomName, direction, client.player, map);
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
        if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.PUZZLE.shape).get(roomName)) != null) {
            room = DebugRoom.ofSinglePossibleRoom(Room.Type.PUZZLE, DungeonMapUtils.getPhysicalRoomPos(player.getPos()), roomName, roomData, direction);
        } else if ((roomData = ROOMS_DATA.get("catacombs").get(Room.Shape.TRAP.shape).get(roomName)) != null) {
            room = DebugRoom.ofSinglePossibleRoom(Room.Type.TRAP, DungeonMapUtils.getPhysicalRoomPos(player.getPos()), roomName, roomData, direction);
        } else if ((roomData = ROOMS_DATA.get("catacombs").values().stream().map(Map::entrySet).flatMap(Collection::stream).filter(entry -> entry.getKey().equals(roomName)).findAny().map(Map.Entry::getValue).orElse(null)) != null) {
            room = DebugRoom.ofSinglePossibleRoom(Room.Type.ROOM, DungeonMapUtils.getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, DungeonMapUtils.getRoomSegments(map, DungeonMapUtils.getMapRoomPos(map, mapEntrancePos, mapRoomSize), mapRoomSize, Room.Type.ROOM.color)), roomName, roomData, direction);
        }
        return room;
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
    @SuppressWarnings("JavadocReference")
    private static void update() {
        if (!Utils.isInDungeons() || isInBoss()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        if (physicalEntrancePos == null) {
            Vec3d playerPos = client.player.getPos();
            physicalEntrancePos = DungeonMapUtils.getPhysicalRoomPos(playerPos);
            currentRoom = newRoom(Room.Type.ENTRANCE, physicalEntrancePos);
        }
        MapState map = FilledMapItem.getMapState(DungeonMap.getMapIdComponent(client.player.getInventory().main.get(8)), client.world);
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
            LOGGER.info("[Skyblocker Dungeon Secrets] Started dungeon with map room size {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, client.player.getPos(), physicalEntrancePos);
        }

        getBloodRushDoorPos(map);

        Vector2ic physicalPos = DungeonMapUtils.getPhysicalRoomPos(client.player.getPos());
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
        currentRoom.tick(client);
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
     * Renders the secret waypoints in {@link #currentRoom} if {@link #shouldProcess()} and {@link #currentRoom} is not null.
     */
    private static void render(WorldRenderContext context) {
        if (shouldProcess() && currentRoom != null) {
            currentRoom.render(context);
        }

        if (bloodRushDoorBox != null && !bloodOpened && SkyblockerConfigManager.get().dungeons.doorHighlight.enableDoorHighlight) {
            float[] colorComponents = hasKey ? GREEN_COLOR_COMPONENTS : RED_COLOR_COMPONENTS;
            switch (SkyblockerConfigManager.get().dungeons.doorHighlight.doorHighlightType) {
                case HIGHLIGHT -> RenderHelper.renderFilled(context, bloodRushDoorBox, colorComponents, 0.5f, true);
                case OUTLINED_HIGHLIGHT -> {
                    RenderHelper.renderFilled(context, bloodRushDoorBox, colorComponents, 0.5f, true);
                    RenderHelper.renderOutline(context, bloodRushDoorBox, colorComponents, 5, true);
                }
                case OUTLINE -> RenderHelper.renderOutline(context, bloodRushDoorBox, colorComponents, 5, true);
            }
        }
    }

    /**
     * Calls {@link Room#onChatMessage(String)} on {@link #currentRoom} if the message is an overlay message and {@link #isCurrentRoomMatched()} and processes key obtained messages.
     * <p>Used to detect when all secrets in a room are found and detect when a wither or blood door is unlocked.
     * To process key obtained messages, this method checks if door highlight is enabled and if the message matches a key obtained message.
     */
    private static void onChatMessage(Text text, boolean overlay) {
        if (!shouldProcess()) {
            return;
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

        var newBoss = DungeonBoss.fromMessage(message);
        if (!isInBoss() && newBoss.isInBoss()) {
            reset();
            boss = newBoss;
        }
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
        Room room = getRoomAtPhysical(itemEntity.getPos());
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
        Room room = getRoomAtPhysical(bat.getPos());
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
     * Checks if {@link DungeonsConfig.SecretWaypoints#enableRoomMatching room matching} is enabled and the player is in a dungeon.
     *
     * @return whether room matching and dungeon secrets should be processed
     */
    private static boolean shouldProcess() {
        return SkyblockerConfigManager.get().dungeons.secretWaypoints.enableRoomMatching && Utils.isInDungeons();
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
}
