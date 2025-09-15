package de.hysky.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.datafixer.fix.ItemIdFix;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Utility class to convert the old dungeon rooms data from Dungeon Rooms Mod to a new format.
 * The new format is similar to <a href="https://quantizr.github.io/posts/how-it-works/">DRM's format</a>, but uses ints instead of longs and a custom numeric block id to store the block states.
 * The first byte is the x position, the second byte is the y position, the third byte is the z position, and the fourth byte is the custom numeric block id.
 * Use {@link DungeonManager#NUMERIC_ID} to get the custom numeric block id of a block.
 * Run this manually when updating dungeon rooms data with DRM's data in {@code src/test/resources/assets/skyblocker/dungeons/dungeonrooms}.
 */
public class DungeonRoomsDFU {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonRoomsDFU.class);
    private static final String DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons";
    private static final String DUNGEON_ROOMS_DATA_DIR = DUNGEONS_DATA_DIR + "/dungeonrooms";
    private static final HashMap<String, HashMap<String, HashMap<String, long[]>>> OLD_ROOMS = new HashMap<>();
    private static final HashMap<String, HashMap<String, HashMap<String, int[]>>> ROOMS = new HashMap<>();

    public static void main(String[] args) {
        load().join();
        updateRooms();
        save().join();
    }

    private static CompletableFuture<Void> load() {
        List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
        URL dungeonsURL = DungeonRoomsDFU.class.getResource(DUNGEON_ROOMS_DATA_DIR);
        if (dungeonsURL == null) {
            LOGGER.error("Failed to load dungeon secrets, unable to find dungeon rooms data directory");
            return CompletableFuture.completedFuture(null);
        }
        Path dungeonsDir = Path.of(dungeonsURL.getPath());
        int resourcePathIndex = dungeonsDir.toString().indexOf(DUNGEON_ROOMS_DATA_DIR);
        try (DirectoryStream<Path> dungeons = Files.newDirectoryStream(dungeonsDir, Files::isDirectory)) {
            for (Path dungeon : dungeons) {
                try (DirectoryStream<Path> roomShapes = Files.newDirectoryStream(dungeon, Files::isDirectory)) {
                    List<CompletableFuture<Void>> roomShapeFutures = new ArrayList<>();
                    HashMap<String, HashMap<String, long[]>> roomShapesMap = new HashMap<>();
                    for (Path roomShape : roomShapes) {
                        roomShapeFutures.add(CompletableFuture.supplyAsync(() -> readRooms(roomShape, resourcePathIndex)).thenAccept(rooms -> roomShapesMap.put(roomShape.getFileName().toString().toLowerCase(Locale.ENGLISH), rooms)));
                    }
                    OLD_ROOMS.put(dungeon.getFileName().toString().toLowerCase(Locale.ENGLISH), roomShapesMap);
                    dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getFileName(), roomShapesMap.size(), roomShapesMap.values().stream().mapToInt(HashMap::size).sum())));
                } catch (IOException e) {
                    LOGGER.error("Failed to load dungeon secrets for dungeon " + dungeon.getFileName(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load dungeon secrets", e);
        }
        return CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", OLD_ROOMS.size(), OLD_ROOMS.values().stream().mapToInt(HashMap::size).sum(), OLD_ROOMS.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
    }

    private static HashMap<String, long[]> readRooms(Path roomShape, int resourcePathIndex) {
        try (DirectoryStream<Path> rooms = Files.newDirectoryStream(roomShape, Files::isRegularFile)) {
            HashMap<String, long[]> roomsData = new HashMap<>();
            for (Path room : rooms) {
                String name = room.getFileName().toString();
                //noinspection DataFlowIssue
                try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(DungeonRoomsDFU.class.getResourceAsStream(room.toString().substring(resourcePathIndex))))) {
                    roomsData.put(name.substring(0, name.length() - 9).toLowerCase(Locale.ENGLISH), (long[]) in.readObject());
                    LOGGER.info("Loaded dungeon secrets room {}", name);
                } catch (NullPointerException | IOException | ClassNotFoundException e) {
                    LOGGER.error("Failed to load dungeon secrets room " + name, e);
                }
            }
            LOGGER.info("Loaded dungeon secrets room shape {} with {} rooms", roomShape.getFileName(), roomsData.size());
            return roomsData;
        } catch (IOException e) {
            LOGGER.error("Failed to load dungeon secrets room shape " + roomShape.getFileName(), e);
        }
        return null;
    }

    private static void updateRooms() {
        for (Map.Entry<String, HashMap<String, HashMap<String, long[]>>> oldDungeon : OLD_ROOMS.entrySet()) {
            HashMap<String, HashMap<String, int[]>> dungeon = new HashMap<>();
            for (Map.Entry<String, HashMap<String, long[]>> oldRoomShape : oldDungeon.getValue().entrySet()) {
                HashMap<String, int[]> roomShape = new HashMap<>();
                for (Map.Entry<String, long[]> oldRoomEntry : oldRoomShape.getValue().entrySet()) {
                    roomShape.put(oldRoomEntry.getKey().replaceAll(" ", "-"), updateRoom(oldRoomEntry.getValue()));
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
        // Technically not needed, as the long array should be sorted already.
        Arrays.sort(room);
        return room;
    }

    /**
     * Updates the block state from Dungeon Rooms Mod's format to the new format explained in {@link DungeonRoomsDFU}.
     *
     * @param oldBlock the old block state in DRM's format
     * @return the new block state in the new format
     */
    private static int updateBlock(long oldBlock) {
        short x = (short) (oldBlock >> 48 & 0xFFFF);
        short y = (short) (oldBlock >> 32 & 0xFFFF);
        short z = (short) (oldBlock >> 16 & 0xFFFF);
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
        return x << 24 | y << 16 | z << 8 | DungeonManager.NUMERIC_ID.getByte(newId);
    }

    private static CompletableFuture<Void> save() {
        List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, HashMap<String, int[]>>> dungeon : ROOMS.entrySet()) {
            Path dungeonDir = Path.of("out", "dungeons", dungeon.getKey());
            List<CompletableFuture<Void>> roomShapeFutures = new ArrayList<>();
            for (Map.Entry<String, HashMap<String, int[]>> roomShape : dungeon.getValue().entrySet()) {
                Path roomShapeDir = dungeonDir.resolve(roomShape.getKey());
                roomShapeFutures.add(CompletableFuture.runAsync(() -> saveRooms(roomShapeDir, roomShape)));
            }
            dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Saved dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getKey(), dungeon.getValue().size(), dungeon.getValue().values().stream().mapToInt(HashMap::size).sum())));
        }
        return CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Saved dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", ROOMS.size(), ROOMS.values().stream().mapToInt(HashMap::size).sum(), ROOMS.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
    }

    private static void saveRooms(Path roomShapeDir, Map.Entry<String, HashMap<String, int[]>> roomShape) {
        try {
            Files.createDirectories(roomShapeDir);
        } catch (IOException e) {
            LOGGER.error("Failed to save dungeon secrets: failed to create dungeon secrets room shape directory " + roomShapeDir, e);
        }
        for (Map.Entry<String, int[]> room : roomShape.getValue().entrySet()) {
            try (ObjectOutputStream out = new ObjectOutputStream(new DeflaterOutputStream(Files.newOutputStream(roomShapeDir.resolve(room.getKey() + ".skeleton"))))) {
                out.writeObject(room.getValue());
                LOGGER.info("Saved dungeon secrets room {}", room.getKey());
            } catch (IOException e) {
                LOGGER.error("Failed to save dungeon secrets room " + room.getKey(), e);
            }
        }
        LOGGER.info("Saved dungeon secrets room shape {} with {} rooms", roomShape.getKey(), roomShape.getValue().size());
    }
}
