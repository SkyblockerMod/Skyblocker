package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class DungeonSecrets {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DungeonSecrets.class);
    private static final String DUNGEONS_PATH = "dungeons";
    /**
     * Maps the block identifier string to a custom numeric block id used in dungeon rooms data.
     *
     * @implNote Not using {@link net.minecraft.registry.Registry#getId(Object) Registry#getId(Block)} and {@link net.minecraft.block.Blocks Blocks} since this is also used by {@link de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU}, which runs outside of Minecraft.
     */
    @SuppressWarnings("JavadocReference")
    protected static final Object2ByteMap<String> NUMERIC_ID = new Object2ByteOpenHashMap<>(Map.ofEntries(
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
    ));
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

    public static boolean isRoomsLoaded() {
        return roomsLoaded != null && roomsLoaded.isDone();
    }

    @SuppressWarnings("unused")
    public static JsonObject getRoomMetadata(String room) {
        return roomsJson.get(room).getAsJsonObject();
    }

    public static JsonArray getRoomWaypoints(String room) {
        return waypointsJson.get(room).getAsJsonArray();
    }

    /**
     * Loads the dungeon secrets asynchronously from {@code /assets/skyblocker/dungeons}.
     * Use {@link #isRoomsLoaded()} to check for completion of loading.
     */
    public static void init() {
        if (SkyblockerConfigManager.get().locations.dungeons.secretWaypoints.noInitSecretWaypoints) {
            return;
        }
        // Execute with MinecraftClient as executor since we need to wait for MinecraftClient#resourceManager to be set
        CompletableFuture.runAsync(DungeonSecrets::load, MinecraftClient.getInstance()).exceptionally(e -> {
            LOGGER.error("[Skyblocker] Failed to load dungeon secrets", e);
            return null;
        });
        Scheduler.INSTANCE.scheduleCyclic(DungeonSecrets::update, 10);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DungeonSecrets::render);
        ClientReceiveMessageEvents.GAME.register(DungeonSecrets::onChatMessage);
        ClientReceiveMessageEvents.GAME_CANCELED.register(DungeonSecrets::onChatMessage);
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onUseBlock(world, hitResult));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("secrets")
                .then(literal("markAsFound").then(markSecretsCommand(true)))
                .then(literal("markAsMissing").then(markSecretsCommand(false)))
                .then(literal("getRelativePos").executes(context -> getRelativePos(context.getSource())))
                .then(literal("getRelativeTargetPos").executes(context -> getRelativeTargetPos(context.getSource())))))));
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> reset()));
    }

    private static void load() {
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> resourceEntry : MinecraftClient.getInstance().getResourceManager().findResources(DUNGEONS_PATH, id -> id.getPath().endsWith(".skeleton")).entrySet()) {
            String[] path = resourceEntry.getKey().getPath().split("/");
            if (path.length != 4) {
                LOGGER.error("[Skyblocker] Failed to load dungeon secrets, invalid resource identifier {}", resourceEntry.getKey());
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
                LOGGER.debug("[Skyblocker] Loaded dungeon secrets dungeon {} room shape {} room {}", dungeon, roomShape, room);
            }).exceptionally(e -> {
                LOGGER.error("[Skyblocker] Failed to load dungeon secrets dungeon {} room shape {} room {}", dungeon, roomShape, room, e);
                return null;
            }));
        }
        dungeonFutures.add(CompletableFuture.runAsync(() -> {
            try (BufferedReader roomsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "dungeons/dungeonrooms.json")); BufferedReader waypointsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "dungeons/secretlocations.json"))) {
                loadJson(roomsReader, roomsJson);
                loadJson(waypointsReader, waypointsJson);
                LOGGER.debug("[Skyblocker] Loaded dungeon secrets json");
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to load dungeon secrets json", e);
            }
        }));
        roomsLoaded = CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("[Skyblocker] Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total in {} ms", ROOMS_DATA.size(), ROOMS_DATA.values().stream().mapToInt(Map::size).sum(), ROOMS_DATA.values().stream().map(Map::values).flatMap(Collection::stream).mapToInt(Map::size).sum(), System.currentTimeMillis() - startTime)).exceptionally(e -> {
            LOGGER.error("[Skyblocker] Failed to load dungeon secrets", e);
            return null;
        });
        LOGGER.info("[Skyblocker] Started loading dungeon secrets in (blocked main thread for) {} ms", System.currentTimeMillis() - startTime);
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

    private static ArgumentBuilder<FabricClientCommandSource, RequiredArgumentBuilder<FabricClientCommandSource, Integer>> markSecretsCommand(boolean found) {
        return argument("secret", IntegerArgumentType.integer()).executes(context -> {
            int secretIndex = IntegerArgumentType.getInteger(context, "secret");
            if (markSecrets(secretIndex, found)) {
                context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable(found ? "skyblocker.dungeons.secrets.markSecretFound" : "skyblocker.dungeons.secrets.markSecretMissing", secretIndex)));
            } else {
                context.getSource().sendError(Constants.PREFIX.get().append(Text.translatable(found ? "skyblocker.dungeons.secrets.markSecretFoundUnable" : "skyblocker.dungeons.secrets.markSecretMissingUnable", secretIndex)));
            }
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int getRelativePos(FabricClientCommandSource source) {
        return getRelativePos(source, source.getPlayer().getBlockPos());
    }

    private static int getRelativeTargetPos(FabricClientCommandSource source) {
        if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            return getRelativePos(source, blockHitResult.getBlockPos());
        } else {
            source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.noTarget")));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getRelativePos(FabricClientCommandSource source, BlockPos pos) {
        if (isCurrentRoomMatched()) {
            BlockPos relativePos = DungeonMapUtils.actualToRelative(currentRoom.getDirection(), currentRoom.getPhysicalCornerPos(), pos);
            source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.posMessage", currentRoom.getName(), relativePos.getX(), relativePos.getY(), relativePos.getZ())));
        } else {
            source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.notMatched")));
        }
        return Command.SINGLE_SUCCESS;
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
     *     <li> Calls {@link Room#update()} on {@link #currentRoom}. </li>
     * </ul>
     */
    @SuppressWarnings("JavadocReference")
    private static void update() {
        if (!SkyblockerConfigManager.get().locations.dungeons.secretWaypoints.enableSecretWaypoints) {
            return;
        }
        if (!Utils.isInDungeons()) {
            if (mapEntrancePos != null) {
                reset();
            }
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }
        if (physicalEntrancePos == null) {
            Vec3d playerPos = player.getPos();
            physicalEntrancePos = DungeonMapUtils.getPhysicalRoomPos(playerPos);
            currentRoom = newRoom(Room.Type.ENTRANCE, physicalEntrancePos);
        }
        ItemStack stack = player.getInventory().main.get(8);
        if (!stack.isOf(Items.FILLED_MAP)) {
            return;
        }
        MapState map = FilledMapItem.getMapState(FilledMapItem.getMapId(stack), client.world);
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
            LOGGER.info("[Skyblocker] Started dungeon with map room size {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, client.player.getPos(), physicalEntrancePos);
        }

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
        currentRoom.update();
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
            LOGGER.error("[Skyblocker] Failed to create room", e);
        }
        return null;
    }

    /**
     * Renders the secret waypoints in {@link #currentRoom} if {@link #isCurrentRoomMatched()}.
     */
    private static void render(WorldRenderContext context) {
        if (isCurrentRoomMatched()) {
            currentRoom.render(context);
        }
    }

    /**
     * Calls {@link Room#onChatMessage(String)} on {@link #currentRoom} if the message is an overlay message and {@link #isCurrentRoomMatched()}.
     * Used to detect when all secrets in a room are found.
     */
    private static void onChatMessage(Text text, boolean overlay) {
        String message = text.getString();

        if (overlay && isCurrentRoomMatched()) {
            currentRoom.onChatMessage(message);
        }

        if (message.equals("[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable.") || message.equals("[BOSS] Scarf: This is where the journey ends for you, Adventurers.")
                || message.equals("[BOSS] The Professor: I was burdened with terrible news recently...") || message.equals("[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!")
                || message.equals("[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.") || message.equals("[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!")
                || message.equals("[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!")) reset();
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
            currentRoom.onUseBlock(world, hitResult);
        }
        return ActionResult.PASS;
    }

    /**
     * Calls {@link Room#onItemPickup(ItemEntity, LivingEntity)} on the room the {@code collector} is in if that room {@link #isRoomMatched(Room)}.
     * Used to detect finding {@link SecretWaypoint.Category.ITEM} secrets.
     * If the collector is the player, {@link #currentRoom} is used as an optimization.
     */
    @SuppressWarnings("JavadocReference")
    public static void onItemPickup(ItemEntity itemEntity, LivingEntity collector, boolean isPlayer) {
        if (isPlayer) {
            if (isCurrentRoomMatched()) {
                currentRoom.onItemPickup(itemEntity, collector);
            }
        } else {
            Room room = getRoomAtPhysical(collector.getPos());
            if (isRoomMatched(room)) {
                room.onItemPickup(itemEntity, collector);
            }
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
     * Calls {@link #isRoomMatched(Room)} on {@link #currentRoom}.
     *
     * @return {@code true} if {@link #currentRoom} is not null and {@link #isRoomMatched(Room)}
     */
    private static boolean isCurrentRoomMatched() {
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
     * Checks if the player is in a dungeon and {@link de.hysky.skyblocker.config.SkyblockerConfig.Dungeons#secretWaypoints Secret Waypoints} is enabled.
     *
     * @return whether dungeon secrets should be processed
     */
    private static boolean shouldProcess() {
        return SkyblockerConfigManager.get().locations.dungeons.secretWaypoints.enableSecretWaypoints && Utils.isInDungeons();
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
    }
}
