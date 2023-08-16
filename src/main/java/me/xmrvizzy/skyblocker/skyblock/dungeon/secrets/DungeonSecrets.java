package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;

public class DungeonSecrets {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DungeonSecrets.class);
    /**
     * Block data for dungeon rooms. See {@link me.xmrvizzy.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU} for format details and how it's generated.
     * All access to this map must check {@link #isRoomsLoaded()} to prevent concurrent modification.
     */
    @SuppressWarnings("JavadocReference")
    protected static final HashMap<String, HashMap<String, HashMap<String, int[]>>> ROOMS_DATA = new HashMap<>();
    /**
     * Maps the block identifier string to a custom numeric block id used in dungeon rooms data.
     *
     * @implNote Not using {@link net.minecraft.registry.Registry#getId(Object) Registry#getId(Block)} and {@link net.minecraft.block.Blocks Blocks} since this is also used by {@link me.xmrvizzy.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU}, which runs outside of Minecraft.
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
    private static final String DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons";
    @NotNull
    private static final Map<Vector2ic, Room> rooms = new HashMap<>();
    private static JsonObject roomsJson;
    private static JsonObject waypointsJson;
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
    public static JsonObject getRoomsJson() {
        return roomsJson;
    }

    public static JsonObject getWaypointsJson() {
        return waypointsJson;
    }

    /**
     * Loads the dungeon secrets asynchronously from {@code /assets/skyblocker/dungeons}.
     * Use {@link #isRoomsLoaded()} to check for completion of loading.
     */
    public static void init() {
        if (SkyblockerConfig.get().locations.dungeons.noInitSecretWaypoints) {
            return;
        }
        CompletableFuture.runAsync(DungeonSecrets::load);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(DungeonSecrets::update, 10);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DungeonSecrets::render);
        ClientReceiveMessageEvents.GAME.register(DungeonSecrets::onChatMessage);
        ClientReceiveMessageEvents.GAME_CANCELED.register(DungeonSecrets::onChatMessage);
        UseBlockCallback.EVENT.register((world, hand, hitResult, hitResult2) -> onUseBlock(hand, hitResult2));
    }

    private static void load() {
        List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
        URL dungeonsURL = SkyblockerMod.class.getResource(DUNGEONS_DATA_DIR);
        if (dungeonsURL == null) {
            LOGGER.error("[Skyblocker] Failed to load dungeon secrets, unable to find dungeon rooms data directory");
            return;
        }
        Path dungeonsDir = Path.of(dungeonsURL.getPath());
        if ("jar".equals(dungeonsURL.getProtocol())) {
            try {
                dungeonsDir = FileSystems.getFileSystem(dungeonsURL.toURI()).getPath(DUNGEONS_DATA_DIR);
            } catch (URISyntaxException e) {
                LOGGER.error("[Skyblocker] Failed to load dungeon secrets, unable to open dungeon rooms data directory", e);
                return;
            }
        }
        int resourcePathIndex = dungeonsDir.toString().indexOf(DUNGEONS_DATA_DIR);
        try (DirectoryStream<Path> dungeons = Files.newDirectoryStream(dungeonsDir, Files::isDirectory)) {
            for (Path dungeon : dungeons) {
                try (DirectoryStream<Path> roomShapes = Files.newDirectoryStream(dungeon, Files::isDirectory)) {
                    List<CompletableFuture<Void>> roomShapeFutures = new ArrayList<>();
                    HashMap<String, HashMap<String, int[]>> roomShapesMap = new HashMap<>();
                    for (Path roomShape : roomShapes) {
                        roomShapeFutures.add(CompletableFuture.supplyAsync(() -> readRooms(roomShape, resourcePathIndex)).thenAccept(rooms -> roomShapesMap.put(roomShape.getFileName().toString(), rooms)));
                    }
                    ROOMS_DATA.put(dungeon.getFileName().toString(), roomShapesMap);
                    dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.debug("[Skyblocker] Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getFileName(), roomShapesMap.size(), roomShapesMap.values().stream().mapToInt(HashMap::size).sum())));
                } catch (IOException e) {
                    LOGGER.error("[Skyblocker] Failed to load dungeon secrets for dungeon " + dungeon.getFileName(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to load dungeon secrets", e);
        }
        // Execute with MinecraftClient as executor since we need to wait for MinecraftClient#resourceManager to be set
        dungeonFutures.add(CompletableFuture.runAsync(() -> {
            try (BufferedReader roomsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "dungeons/dungeonrooms.json")); BufferedReader waypointsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "dungeons/secretlocations.json"))) {
                roomsJson = SkyblockerMod.GSON.fromJson(roomsReader, JsonObject.class);
                waypointsJson = SkyblockerMod.GSON.fromJson(waypointsReader, JsonObject.class);
                LOGGER.debug("[Skyblocker] Loaded dungeon secrets json");
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to load dungeon secrets json", e);
            }
        }, MinecraftClient.getInstance()));
        roomsLoaded = CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("[Skyblocker] Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", ROOMS_DATA.size(), ROOMS_DATA.values().stream().mapToInt(HashMap::size).sum(), ROOMS_DATA.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
    }

    private static HashMap<String, int[]> readRooms(Path roomShape, int resourcePathIndex) {
        try (DirectoryStream<Path> rooms = Files.newDirectoryStream(roomShape, Files::isRegularFile)) {
            HashMap<String, int[]> roomsData = new HashMap<>();
            for (Path room : rooms) {
                String name = room.getFileName().toString();
                //noinspection DataFlowIssue
                try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(SkyblockerMod.class.getResourceAsStream(room.toString().substring(resourcePathIndex))))) {
                    roomsData.put(name.substring(0, name.length() - 9), (int[]) in.readObject());
                    LOGGER.debug("[Skyblocker] Loaded dungeon secrets room {}", name);
                } catch (NullPointerException | IOException | ClassNotFoundException e) {
                    LOGGER.error("[Skyblocker] Failed to load dungeon secrets room " + name, e);
                }
            }
            LOGGER.debug("[Skyblocker] Loaded dungeon secrets room shape {} with {} rooms", roomShape.getFileName(), roomsData.size());
            return roomsData;
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to load dungeon secrets room shape " + roomShape.getFileName(), e);
        }
        return null;
    }

    /**
     * Updates the dungeon. The general idea is similar to the Dungeon Rooms Mod.
     * <p></p>
     * When entering a new dungeon, this method:
     * <ul>
     *     <li> Gets the upper left corner of entrance room on the map and saves it in {@link #mapEntrancePos}. </li>
     *     <li> Gets the size of a room on the map in pixels and saves it in {@link #mapRoomSize}. </li>
     *     <li> Gets the physical northwest corner position of the entrance room and saves it in {@link #physicalEntrancePos}. </li>
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
        if (!SkyblockerConfig.get().locations.dungeons.secretWaypoints) {
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
        ItemStack stack = player.getInventory().main.get(8);
        if (!stack.isOf(Items.FILLED_MAP)) {
            return;
        }
        MapState map = FilledMapItem.getMapState(FilledMapItem.getMapId(stack), client.world);
        if (map == null) {
            return;
        }
        if (mapEntrancePos == null && (mapEntrancePos = DungeonMapUtils.getMapEntrancePos(map)) == null) {
            return;
        }
        if (mapRoomSize == 0 && (mapRoomSize = DungeonMapUtils.getMapRoomSize(map, mapEntrancePos)) == 0) {
            return;
        }
        if (physicalEntrancePos == null) {
            physicalEntrancePos = DungeonMapUtils.getPhysicalEntrancePos(map, player.getPos());
            if (physicalEntrancePos == null) {
                player.sendMessage(Text.translatable("skyblocker.dungeons.secrets.physicalEntranceNotFound"));
                return;
            } else {
                currentRoom = newRoom(Room.Type.ENTRANCE, physicalEntrancePos);
                LOGGER.info("[Skyblocker] Started dungeon with map room size {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, client.player.getPos(), physicalEntrancePos);
            }
        }

        Vector2ic physicalPos = DungeonMapUtils.getPhysicalRoomPos(client.player.getPos());
        Vector2ic mapPos = DungeonMapUtils.getMapPosFromPhysical(physicalEntrancePos, mapEntrancePos, mapRoomSize, physicalPos);
        Room.Type type = DungeonMapUtils.getRoomType(map, mapPos);
        if (type == null) {
            return;
        }
        Room room = rooms.get(physicalPos);
        if (room == null) {
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

    private static void render(WorldRenderContext context) {
        if (isCurrentRoomMatched()) {
            currentRoom.render(context);
        }
    }

    private static void onChatMessage(Text text, boolean overlay) {
        if (overlay && isCurrentRoomMatched()) {
            currentRoom.onChatMessage(text.getString());
        }
    }

    private static ActionResult onUseBlock(World world, BlockHitResult hitResult) {
        if (isCurrentRoomMatched()) {
            currentRoom.onUseBlock(world, hitResult);
        }
        return ActionResult.PASS;
    }

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

    @Nullable
    private static Room getRoomAtPhysical(Vec3d pos) {
        return rooms.get(DungeonMapUtils.getPhysicalRoomPos(pos));
    }

    private static boolean isCurrentRoomMatched() {
        return isRoomMatched(currentRoom);
    }

    @Contract("null -> false")
    private static boolean isRoomMatched(@Nullable Room room) {
        return shouldProcess() && room != null && room.isMatched();
    }

    private static boolean shouldProcess() {
        return SkyblockerConfig.get().locations.dungeons.secretWaypoints && Utils.isInDungeons();
    }

    private static void reset() {
        mapEntrancePos = null;
        mapRoomSize = 0;
        physicalEntrancePos = null;
        rooms.clear();
        currentRoom = null;
    }
}
