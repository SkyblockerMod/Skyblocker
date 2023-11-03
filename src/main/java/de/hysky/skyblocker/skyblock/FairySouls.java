package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FairySouls {
    private static final Logger LOGGER = LoggerFactory.getLogger(FairySouls.class);
    private static CompletableFuture<Void> fairySoulsLoaded;
    private static int maxSouls = 0;
    private static final Map<String, Set<BlockPos>> fairySouls = new HashMap<>();
    private static final Map<String, Map<String, Set<BlockPos>>> foundFairies = new HashMap<>();

    @SuppressWarnings("UnusedReturnValue")
    public static CompletableFuture<Void> runAsyncAfterFairySoulsLoad(Runnable runnable) {
        if (fairySoulsLoaded == null) {
            LOGGER.error("Fairy Souls have not being initialized yet! Please ensure the Fairy Souls module is initialized before modules calling this method in SkyblockerMod#onInitializeClient. This error can be safely ignore in a test environment.");
            return CompletableFuture.completedFuture(null);
        }
        return fairySoulsLoaded.thenRunAsync(runnable);
    }

    public static int getFairySoulsSize(@Nullable String location) {
        return location == null ? maxSouls : fairySouls.get(location).size();
    }

    public static void init() {
        loadFairySouls();
        ClientLifecycleEvents.CLIENT_STOPPING.register(FairySouls::saveFoundFairySouls);
        ClientCommandRegistrationCallback.EVENT.register(FairySouls::registerCommands);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(FairySouls::render);
        ClientReceiveMessageEvents.GAME.register(FairySouls::onChatMessage);
    }

    private static void loadFairySouls() {
        fairySoulsLoaded = NEURepoManager.runAsyncAfterLoad(() -> {
            maxSouls = NEURepoManager.NEU_REPO.getConstants().getFairySouls().getMaxSouls();
            NEURepoManager.NEU_REPO.getConstants().getFairySouls().getSoulLocations().forEach((location, fairySoulsForLocation) -> fairySouls.put(location, fairySoulsForLocation.stream().map(coordinate -> new BlockPos(coordinate.getX(), coordinate.getY(), coordinate.getZ())).collect(Collectors.toUnmodifiableSet())));
            LOGGER.debug("[Skyblocker] Loaded {} fairy souls across {} locations", fairySouls.values().stream().mapToInt(Set::size).sum(), fairySouls.size());

            try (BufferedReader reader = Files.newBufferedReader(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json"))) {
                for (Map.Entry<String, JsonElement> foundFairiesForProfileJson : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    Map<String, Set<BlockPos>> foundFairiesForProfile = new HashMap<>();
                    for (Map.Entry<String, JsonElement> foundFairiesForLocationJson : foundFairiesForProfileJson.getValue().getAsJsonObject().asMap().entrySet()) {
                        Set<BlockPos> foundFairiesForLocation = new HashSet<>();
                        for (JsonElement foundFairy : foundFairiesForLocationJson.getValue().getAsJsonArray().asList()) {
                            foundFairiesForLocation.add(PosUtils.parsePosString(foundFairy.getAsString()));
                        }
                        foundFairiesForProfile.put(foundFairiesForLocationJson.getKey(), foundFairiesForLocation);
                    }
                    foundFairies.put(foundFairiesForProfileJson.getKey(), foundFairiesForProfile);
                }
                LOGGER.debug("[Skyblocker] Loaded found fairy souls");
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load found fairy souls", e);
            }
            LOGGER.info("[Skyblocker] Loaded {} fairy souls across {} locations and {} found fairy souls across {} locations in {} profiles", fairySouls.values().stream().mapToInt(Set::size).sum(), fairySouls.size(), foundFairies.values().stream().map(Map::values).flatMap(Collection::stream).mapToInt(Set::size).sum(), foundFairies.values().stream().mapToInt(Map::size).sum(), foundFairies.size());
        });
    }

    private static void saveFoundFairySouls(MinecraftClient client) {
        try (BufferedWriter writer = Files.newBufferedWriter(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json"))) {
            JsonObject foundFairiesJson = new JsonObject();
            for (Map.Entry<String, Map<String, Set<BlockPos>>> foundFairiesForProfile : foundFairies.entrySet()) {
                JsonObject foundFairiesForProfileJson = new JsonObject();
                for (Map.Entry<String, Set<BlockPos>> foundFairiesForLocation : foundFairiesForProfile.getValue().entrySet()) {
                    JsonArray foundFairiesForLocationJson = new JsonArray();
                    for (BlockPos foundFairy : foundFairiesForLocation.getValue()) {
                        foundFairiesForLocationJson.add(PosUtils.getPosString(foundFairy));
                    }
                    foundFairiesForProfileJson.add(foundFairiesForLocation.getKey(), foundFairiesForLocationJson);
                }
                foundFairiesJson.add(foundFairiesForProfile.getKey(), foundFairiesForProfileJson);
            }
            SkyblockerMod.GSON.toJson(foundFairiesJson, writer);
            writer.close();
            LOGGER.info("[Skyblocker] Saved found fairy souls");
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to write found fairy souls to file", e);
        }
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("fairySouls")
                        .then(literal("markAllInCurrentIslandFound").executes(context -> {
                            FairySouls.markAllFairiesOnCurrentIslandFound();
                            context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.fairySouls.markAllFound")));
                            return 1;
                        }))
                        .then(literal("markAllInCurrentIslandMissing").executes(context -> {
                            FairySouls.markAllFairiesOnCurrentIslandMissing();
                            context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.fairySouls.markAllMissing")));
                            return 1;
                        }))));
    }

    private static void render(WorldRenderContext context) {
        SkyblockerConfig.FairySouls fairySoulsConfig = SkyblockerConfigManager.get().general.fairySouls;

        if (fairySoulsConfig.enableFairySoulsHelper && fairySoulsLoaded.isDone() && fairySouls.containsKey(Utils.getLocationRaw())) {
            for (BlockPos fairySoulPos : fairySouls.get(Utils.getLocationRaw())) {
                boolean fairySoulNotFound = isFairySoulMissing(fairySoulPos);
                if (!fairySoulsConfig.highlightFoundSouls && !fairySoulNotFound || fairySoulsConfig.highlightOnlyNearbySouls && fairySoulPos.getSquaredDistance(context.camera().getPos()) > 2500) {
                    continue;
                }
                float[] colorComponents = fairySoulNotFound ? DyeColor.GREEN.getColorComponents() : DyeColor.RED.getColorComponents();
                RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, fairySoulPos, colorComponents, 0.5F);
            }
        }
    }

    private static void onChatMessage(Text text, boolean overlay) {
        String message = text.getString();
        if (message.equals("You have already found that Fairy Soul!") || message.equals("§d§lSOUL! §fYou found a §dFairy Soul§f!")) {
            markClosestFairyFound();
        }
    }

    private static void markClosestFairyFound() {
        if (!fairySoulsLoaded.isDone()) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            LOGGER.warn("[Skyblocker] Failed to mark closest fairy soul as found because player is null");
            return;
        }
        fairySouls.get(Utils.getLocationRaw()).stream()
                .filter(FairySouls::isFairySoulMissing)
                .min(Comparator.comparingDouble(fairySoulPos -> fairySoulPos.getSquaredDistance(player.getPos())))
                .filter(fairySoulPos -> fairySoulPos.getSquaredDistance(player.getPos()) <= 16)
                .ifPresent(fairySoulPos -> {
                    initializeFoundFairiesForCurrentProfileAndLocation();
                    foundFairies.get(Utils.getProfile()).get(Utils.getLocationRaw()).add(fairySoulPos);
                });
    }

    private static boolean isFairySoulMissing(BlockPos fairySoulPos) {
        Map<String, Set<BlockPos>> foundFairiesForProfile = foundFairies.get(Utils.getProfile());
        if (foundFairiesForProfile == null) {
            return true;
        }
        Set<BlockPos> foundFairiesForProfileAndLocation = foundFairiesForProfile.get(Utils.getLocationRaw());
        if (foundFairiesForProfileAndLocation == null) {
            return true;
        }
        return !foundFairiesForProfileAndLocation.contains(fairySoulPos);
    }

    public static void markAllFairiesOnCurrentIslandFound() {
        initializeFoundFairiesForCurrentProfileAndLocation();
        foundFairies.get(Utils.getProfile()).get(Utils.getLocationRaw()).addAll(fairySouls.get(Utils.getLocationRaw()));
    }

    public static void markAllFairiesOnCurrentIslandMissing() {
        Map<String, Set<BlockPos>> foundFairiesForProfile = foundFairies.get(Utils.getProfile());
        if (foundFairiesForProfile != null) {
            foundFairiesForProfile.remove(Utils.getLocationRaw());
        }
    }

    private static void initializeFoundFairiesForCurrentProfileAndLocation() {
        initializeFoundFairiesForProfileAndLocation(Utils.getProfile(), Utils.getLocationRaw());
    }

    private static void initializeFoundFairiesForProfileAndLocation(String profile, String location) {
        foundFairies.computeIfAbsent(profile, profileKey -> new HashMap<>());
        foundFairies.get(profile).computeIfAbsent(location, locationKey -> new HashSet<>());
    }
}
