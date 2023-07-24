package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    private static final String DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons";
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
    private static JsonObject roomsJson;
    private static JsonObject waypointsJson;
    @Nullable
    private static CompletableFuture<Void> roomsLoaded;
    /**
     * The map position of the top left corner of the entrance room.
     */
    private static Vector2ic mapEntrancePos;
    /**
     * The width of a room on the map.
     */
    private static int mapRoomSize;
    /**
     * The physical position of the northwest corner of the entrance room.
     */
    private static Vector2ic physicalEntrancePos;
    private static final Map<Vector2ic, Room> rooms = new HashMap<>();
    private static Room currentRoom;

    public static boolean isRoomsLoaded() {
        return roomsLoaded != null && roomsLoaded.isDone();
    }

    /**
     * Loads the dungeon secrets asynchronously from {@code /assets/skyblocker/dungeons}.
     * Use {@link #isRoomsLoaded()} to check for completion of loading.
     */
    public static void init() {
        if (SkyblockerConfig.get().locations.dungeons.noLoadSecretWaypoints) {
            return;
        }
        CompletableFuture.runAsync(DungeonSecrets::load);
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(DungeonSecrets::update, 10);
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

    private static void update() {
        if (!SkyblockerConfig.get().locations.dungeons.secretWaypoints || !Utils.isInDungeons()) {
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
                LOGGER.info("[Skyblocker] Started dungeon with map room width {}, map entrance pos {}, player pos {}, and physical entrance pos {}", mapRoomSize, mapEntrancePos, client.player.getPos(), physicalEntrancePos);
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
    private static Room newRoom(Room.Type type, Vector2ic... physicalPositions) {
        Room newRoom = new Room(type, physicalPositions);
        for (Vector2ic physicalPos : physicalPositions) {
            rooms.put(physicalPos, newRoom);
        }
        return newRoom;
    }
}
