package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;

public class DungeonRoomsDFU {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonRoomsDFU.class);
    private static final String DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons";
    private static final String DUNGEON_ROOMS_DATA_DIR = DUNGEONS_DATA_DIR + "/dungeonrooms";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HashMap<String, HashMap<String, HashMap<String, long[]>>> ROOMS = new HashMap<>();
    private static JsonObject roomsJson;
    private static JsonObject waypointsJson;

    public static void main(String[] args) {
        load().join();
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
                ROOMS.put(dungeon.getName(), new HashMap<>());
                List<CompletableFuture<Void>> roomShapeFutures = new ArrayList<>();
                for (File roomShape : roomShapes) {
                    roomShapeFutures.add(CompletableFuture.supplyAsync(() -> readRooms(roomShape, resourcePathIndex)).thenAccept(rooms -> ROOMS.get(dungeon.getName()).put(roomShape.getName(), rooms)));
                }
                dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getName(), ROOMS.get(dungeon.getName()).size(), ROOMS.get(dungeon.getName()).values().stream().mapToInt(HashMap::size).sum())));
            }
            dungeonFutures.add(CompletableFuture.runAsync(() -> {
                //noinspection DataFlowIssue
                try (BufferedReader roomsReader = new BufferedReader(new InputStreamReader(DungeonRoomsDFU.class.getResourceAsStream(DUNGEONS_DATA_DIR + "/dungeonrooms.json")));
                     BufferedReader waypointsReader = new BufferedReader(new InputStreamReader(DungeonRoomsDFU.class.getResourceAsStream(DUNGEONS_DATA_DIR + "/secretlocations.json")))) {
                    roomsJson = GSON.fromJson(roomsReader, JsonObject.class);
                    waypointsJson = GSON.fromJson(waypointsReader, JsonObject.class);
                    LOGGER.info("Loaded dungeon secrets json");
                } catch (Exception e) {
                    LOGGER.error("Failed to load dungeon secrets json", e);
                }
            }));
            return CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", ROOMS.size(), ROOMS.values().stream().mapToInt(HashMap::size).sum(), ROOMS.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
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
}
