package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;

public class DungeonSecrets {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonSecrets.class);
    private static final String DUNGEONS_DATA_DIR = "/assets/skyblocker/dungeons";
    private static final HashMap<String, HashMap<String, HashMap<String, long[]>>> ROOMS = new HashMap<>();
    private static JsonObject roomsJson;
    private static JsonObject waypointsJson;
    @Nullable
    private static CompletableFuture<Void> roomsLoaded;

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
        CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> dungeonFutures = new ArrayList<>();
                //noinspection DataFlowIssue
                File dungeons = new File(SkyblockerMod.class.getResource(DUNGEONS_DATA_DIR).getFile());
                int resourcePathIndex = dungeons.getPath().indexOf(DUNGEONS_DATA_DIR);
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
                    dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.debug("Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getName(), ROOMS.get(dungeon.getName()).size(), ROOMS.get(dungeon.getName()).values().stream().mapToInt(HashMap::size).sum())));
                }
                // Execute with MinecraftClient as executor since we need to wait for MinecraftClient#resourceManager to be set
                dungeonFutures.add(CompletableFuture.runAsync(() -> {
                    try (BufferedReader roomsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "dungeons/dungeonrooms.json")); BufferedReader waypointsReader = MinecraftClient.getInstance().getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "dungeons/secretlocations.json"))) {
                        roomsJson = SkyblockerMod.GSON.fromJson(roomsReader, JsonObject.class);
                        waypointsJson = SkyblockerMod.GSON.fromJson(waypointsReader, JsonObject.class);
                        LOGGER.debug("Loaded dungeon secrets json");
                    } catch (Exception e) {
                        LOGGER.error("Failed to load dungeon secrets json", e);
                    }
                }, MinecraftClient.getInstance()));
                roomsLoaded = CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", ROOMS.size(), ROOMS.values().stream().mapToInt(HashMap::size).sum(), ROOMS.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
            } catch (Exception e) {
                LOGGER.error("Failed to load dungeon secrets", e);
            }
        });
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
            try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(SkyblockerMod.class.getResourceAsStream(room.getPath().substring(resourcePathIndex))))) {
                data.put(name.substring(0, name.length() - 9), (long[]) in.readObject());
                LOGGER.debug("Loaded dungeon secrets room {}", name);
            } catch (NullPointerException | IOException | ClassNotFoundException e) {
                LOGGER.error("Failed to load dungeon secrets room " + name, e);
            }
        }
        LOGGER.debug("Loaded dungeon secrets room shape {} with {} rooms", roomShape.getName(), data.size());
        return data;
    }
}
