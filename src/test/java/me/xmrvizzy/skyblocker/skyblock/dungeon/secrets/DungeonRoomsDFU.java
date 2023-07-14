package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.datafixer.fix.ItemIdFix;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;

/**
 * Utility class to convert the old dungeon rooms data from Dungeon Rooms Mod to a new format.
 * The new format is similar to <a href="https://quantizr.github.io/posts/how-it-works/">DRM's format</a>, but uses ints instead of longs and a custom numeric block id to store the block states.
 * The first byte is the x position, the second byte is the y position, the third byte is the z position, and the fourth byte is the custom numeric block id.
 * Use {@link DungeonSecrets#NUMERIC_ID} to get the custom numeric block id of a block.
 * Run this manually when updating dungeon rooms data with DRM's data in {@code src/test/resources/assets/skyblocker/dungeons/dungeonrooms}.
 */
public class DungeonRoomsDFU {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonRoomsDFU.class);
    private static final String DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons";
    private static final String DUNGEON_ROOMS_DATA_DIR = DUNGEONS_DATA_DIR + "/dungeonrooms";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HashMap<String, HashMap<String, HashMap<String, long[]>>> OLD_ROOMS = new HashMap<>();
    private static final HashMap<String, HashMap<String, HashMap<String, int[]>>> ROOMS = new HashMap<>();
    private static JsonObject roomsJson;
    private static JsonObject waypointsJson;

    public static void main(String[] args) {
        load().join();
        updateRooms();
    }

    private static CompletableFuture<Void> load() {
        try {
            List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
            //noinspection DataFlowIssue
            File dungeons = new File(DungeonRoomsDFU.class.getResource(DUNGEON_ROOMS_DATA_DIR).getFile());
            int resourcePathIndex = dungeons.getPath().indexOf(DUNGEON_ROOMS_DATA_DIR);
            //noinspection DataFlowIssue
            for (File dungeon : dungeons.listFiles()) {
                if (!dungeon.isDirectory()) {
                    continue;
                }
                File[] roomShapes = dungeon.listFiles();
                if (roomShapes == null) {
                    LOGGER.error("Failed to load dungeon secrets for dungeon {}", dungeon.getName());
                    continue;
                }
                OLD_ROOMS.put(dungeon.getName(), new HashMap<>());
                List<CompletableFuture<Void>> roomShapeFutures = new ArrayList<>();
                for (File roomShape : roomShapes) {
                    roomShapeFutures.add(CompletableFuture.supplyAsync(() -> readRooms(roomShape, resourcePathIndex)).thenAccept(rooms -> OLD_ROOMS.get(dungeon.getName()).put(roomShape.getName(), rooms)));
                }
                dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getName(), OLD_ROOMS.get(dungeon.getName()).size(), OLD_ROOMS.get(dungeon.getName()).values().stream().mapToInt(HashMap::size).sum())));
            }
            dungeonFutures.add(CompletableFuture.runAsync(() -> {
                //noinspection DataFlowIssue
                try (BufferedReader roomsReader = new BufferedReader(new InputStreamReader(DungeonRoomsDFU.class.getResourceAsStream(DUNGEONS_DATA_DIR + "/dungeonrooms.json"))); BufferedReader waypointsReader = new BufferedReader(new InputStreamReader(DungeonRoomsDFU.class.getResourceAsStream(DUNGEONS_DATA_DIR + "/secretlocations.json")))) {
                    roomsJson = GSON.fromJson(roomsReader, JsonObject.class);
                    waypointsJson = GSON.fromJson(waypointsReader, JsonObject.class);
                    LOGGER.info("Loaded dungeon secrets json");
                } catch (Exception e) {
                    LOGGER.error("Failed to load dungeon secrets json", e);
                }
            }));
            return CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", OLD_ROOMS.size(), OLD_ROOMS.values().stream().mapToInt(HashMap::size).sum(), OLD_ROOMS.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dungeon secrets", e);
        }
    }

    private static HashMap<String, long[]> readRooms(File roomShape, int resourcePathIndex) {
        HashMap<String, long[]> data = new HashMap<>();
        File[] rooms = roomShape.listFiles();
        if (rooms == null) {
            LOGGER.error("Failed to load dungeon secrets room shape {}", roomShape.getName());
            return data;
        }
        for (File room : rooms) {
            String name = room.getName();
            //noinspection DataFlowIssue
            try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(DungeonRoomsDFU.class.getResourceAsStream(room.getPath().substring(resourcePathIndex))))) {
                data.put(name.substring(0, name.length() - 9), (long[]) in.readObject());
                LOGGER.info("Loaded dungeon secrets room {}", name);
            } catch (NullPointerException | IOException | ClassNotFoundException e) {
                LOGGER.error("Failed to load dungeon secrets room " + name, e);
            }
        }
        LOGGER.info("Loaded dungeon secrets room shape {} with {} rooms", roomShape.getName(), data.size());
        return data;
    }

    private static void updateRooms() {
        for (Map.Entry<String, HashMap<String, HashMap<String, long[]>>> oldDungeon : OLD_ROOMS.entrySet()) {
            HashMap<String, HashMap<String, int[]>> dungeon = new HashMap<>();
            for (Map.Entry<String, HashMap<String, long[]>> oldRoomShape : oldDungeon.getValue().entrySet()) {
                HashMap<String, int[]> roomShape = new HashMap<>();
                for (Map.Entry<String, long[]> oldRoomEntry : oldRoomShape.getValue().entrySet()) {
                    roomShape.put(oldRoomEntry.getKey(), updateRoom(oldRoomEntry.getValue()));
                }
                dungeon.put(oldRoomShape.getKey(), roomShape);
            }
            ROOMS.put(oldDungeon.getKey(), dungeon);
        }
    }

    private static int[] updateRoom(long[] oldRoom) {
        int[] room = new int[oldRoom.length];
        for (int i = 0; i < oldRoom.length; i++) {
            room[i] = updateBlock(oldRoom[i]);
        }
        return room;
    }

    /**
     * Updates the block state from Dungeon Rooms Mod's format to the new format explained in {@link DungeonRoomsDFU}.
     *
     * @param oldBlock the old block state in DRM's format
     * @return the new block state in the new format
     */
    private static int updateBlock(long oldBlock) {
        short x = (short) ((oldBlock >> 48) & 0xFFFF);
        short y = (short) ((oldBlock >> 32) & 0xFFFF);
        short z = (short) ((oldBlock >> 16) & 0xFFFF);
        // Blocks should be within the range 0 to 256, since a dungeon room is at most around 128 blocks long and around 150 blocks tall.
        if (x < 0 || x > 0xFF || y < 0 || y > 0xFF || z < 0 || z > 0xFF) {
            throw new IllegalArgumentException("Invalid block: " + oldBlock);
        }
        short oldId = (short) (oldBlock & 0xFFFF);
        // Get the new id for the block.
        String newId = ItemInstanceTheFlatteningFix.getItem(ItemIdFix.fromId(oldId / 100), oldId % 100);
        if (newId == null) {
            newId = ItemIdFix.fromId(oldId / 100);
        }
        return (x << 24) | (y << 16) | (z << 8) | DungeonSecrets.NUMERIC_ID.get(newId);
    }
}
