package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
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
    private static final HashMap<String, HashMap<String, HashMap<String, int[]>>> ROOMS = new HashMap<>();
    /**
     * Maps the block identifier string to a custom numeric block id used in dungeon rooms data.
     *
     * @implNote Not using {@link net.minecraft.registry.Registry#getId(Object) Registry#getId(Block)} and {@link net.minecraft.block.Blocks Blocks} since this is also used by {@link me.xmrvizzy.skyblocker.skyblock.dungeon.secrets.DungeonRoomsDFU DungeonRoomsDFU}, which runs outside of Minecraft.
     */
    @SuppressWarnings("JavadocReference")
    protected static final Map<String, Byte> NUMERIC_ID = Map.ofEntries(Map.entry("minecraft:stone", (byte) 1), Map.entry("minecraft:diorite", (byte) 2), Map.entry("minecraft:polished_diorite", (byte) 3), Map.entry("minecraft:andesite", (byte) 4), Map.entry("minecraft:polished_andesite", (byte) 5), Map.entry("minecraft:grass_block", (byte) 6), Map.entry("minecraft:dirt", (byte) 7), Map.entry("minecraft:coarse_dirt", (byte) 8), Map.entry("minecraft:cobblestone", (byte) 9), Map.entry("minecraft:bedrock", (byte) 10), Map.entry("minecraft:oak_leaves", (byte) 11), Map.entry("minecraft:gray_wool", (byte) 12), Map.entry("minecraft:double_stone_slab", (byte) 13), Map.entry("minecraft:mossy_cobblestone", (byte) 14), Map.entry("minecraft:clay", (byte) 15), Map.entry("minecraft:stone_bricks", (byte) 16), Map.entry("minecraft:mossy_stone_bricks", (byte) 17), Map.entry("minecraft:chiseled_stone_bricks", (byte) 18), Map.entry("minecraft:gray_terracotta", (byte) 19), Map.entry("minecraft:cyan_terracotta", (byte) 20), Map.entry("minecraft:black_terracotta", (byte) 21));
    private static JsonObject roomsJson;
    private static JsonObject waypointsJson;
    @Nullable
    private static CompletableFuture<Void> roomsLoaded;
    private static Vector2ic mapEntrancePos;
    private static int mapRoomWidth;
    private static Vector2ic physicalEntrancePos;
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
            LOGGER.error("Failed to load dungeon secrets, unable to find dungeon rooms data directory");
            return;
        }
        Path dungeonsDir = Path.of(dungeonsURL.getPath());
        if ("jar".equals(dungeonsURL.getProtocol())) {
            try {
                dungeonsDir = FileSystems.getFileSystem(dungeonsURL.toURI()).getPath(DUNGEONS_DATA_DIR);
            } catch (URISyntaxException e) {
                LOGGER.error("Failed to load dungeon secrets, unable to open dungeon rooms data directory", e);
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
                    ROOMS.put(dungeon.getFileName().toString(), roomShapesMap);
                    dungeonFutures.add(CompletableFuture.allOf(roomShapeFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.debug("Loaded dungeon secrets for dungeon {} with {} room shapes and {} rooms total", dungeon.getFileName(), roomShapesMap.size(), roomShapesMap.values().stream().mapToInt(HashMap::size).sum())));
                } catch (IOException e) {
                    LOGGER.error("Failed to load dungeon secrets for dungeon " + dungeon.getFileName(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load dungeon secrets", e);
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
        roomsLoaded = CompletableFuture.allOf(dungeonFutures.toArray(CompletableFuture[]::new)).thenRun(() -> LOGGER.info("[Skyblocker] Loaded dungeon secrets for {} dungeon(s), {} room shapes, and {} rooms total", ROOMS.size(), ROOMS.values().stream().mapToInt(HashMap::size).sum(), ROOMS.values().stream().map(HashMap::values).flatMap(Collection::stream).mapToInt(HashMap::size).sum()));
    }

    private static HashMap<String, int[]> readRooms(Path roomShape, int resourcePathIndex) {
        try (DirectoryStream<Path> rooms = Files.newDirectoryStream(roomShape, Files::isRegularFile)) {
            HashMap<String, int[]> roomsData = new HashMap<>();
            for (Path room : rooms) {
                String name = room.getFileName().toString();
                //noinspection DataFlowIssue
                try (ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(SkyblockerMod.class.getResourceAsStream(room.toString().substring(resourcePathIndex))))) {
                    roomsData.put(name.substring(0, name.length() - 9), (int[]) in.readObject());
                    LOGGER.debug("Loaded dungeon secrets room {}", name);
                } catch (NullPointerException | IOException | ClassNotFoundException e) {
                    LOGGER.error("Failed to load dungeon secrets room " + name, e);
                }
            }
            LOGGER.debug("Loaded dungeon secrets room shape {} with {} rooms", roomShape.getFileName(), roomsData.size());
            return roomsData;
        } catch (IOException e) {
            LOGGER.error("Failed to load dungeon secrets room shape " + roomShape.getFileName(), e);
        }
        return null;
    }

    private static void update() {
        if (!SkyblockerConfig.get().locations.dungeons.secretWaypoints || !Utils.isInDungeons()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        ItemStack stack = client.player.getInventory().main.get(8);
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
        if (mapRoomWidth == 0 && (mapRoomWidth = DungeonMapUtils.getMapRoomWidth(map, mapEntrancePos)) == 0) {
            return;
        }
        if (physicalEntrancePos == null && (physicalEntrancePos = DungeonMapUtils.getPhysicalEntrancePos(map, client.player.getPos())) == null) {
            client.player.sendMessage(Text.translatable("skyblocker.dungeons.secrets.physicalEntranceNotFound"));
            return;
        }
        LOGGER.info("[Skyblocker] Detected dungeon with map room width {} and entrance at map pos {} and physical pos {}", mapRoomWidth, mapEntrancePos, physicalEntrancePos);
    }
}
