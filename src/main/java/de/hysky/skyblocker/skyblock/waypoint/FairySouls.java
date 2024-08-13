package de.hysky.skyblocker.skyblock.waypoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FairySouls {
    private static final Logger LOGGER = LoggerFactory.getLogger(FairySouls.class);
    private static final Supplier<Waypoint.Type> TYPE_SUPPLIER = () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType;
    private static CompletableFuture<Void> fairySoulsLoaded;
    private static int maxSouls = 0;
    private static final Map<String, Map<BlockPos, ProfileAwareWaypoint>> fairySouls = new HashMap<>();

    @SuppressWarnings("UnusedReturnValue")
    public static CompletableFuture<Void> runAsyncAfterFairySoulsLoad(Runnable runnable) {
        if (fairySoulsLoaded == null) {
            LOGGER.error("[Skyblocker] Fairy Souls have not being initialized yet! Please ensure the Fairy Souls configs is initialized before modules calling this method in SkyblockerMod#onInitializeClient. This error can be safely ignore in a test environment.");
            return CompletableFuture.completedFuture(null);
        }
        return fairySoulsLoaded.thenRunAsync(runnable);
    }

    public static int getFairySoulsSize(@Nullable String location) {
        return location == null ? maxSouls : fairySouls.get(location).size();
    }

    @Init
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
            NEURepoManager.NEU_REPO.getConstants().getFairySouls().getSoulLocations().forEach((location, fairiesForLocation) -> fairySouls.put(location, fairiesForLocation.stream().map(coordinate -> new BlockPos(coordinate.getX(), coordinate.getY(), coordinate.getZ())).collect(Collectors.toUnmodifiableMap(pos -> pos, pos -> new ProfileAwareWaypoint(pos, TYPE_SUPPLIER, ColorUtils.getFloatComponents(DyeColor.GREEN), ColorUtils.getFloatComponents(DyeColor.RED))))));
            LOGGER.debug("[Skyblocker] Loaded {} fairy souls across {} locations", fairySouls.values().stream().mapToInt(Map::size).sum(), fairySouls.size());

            try (BufferedReader reader = Files.newBufferedReader(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json"))) {
                for (Map.Entry<String, JsonElement> foundFairiesForProfileJson : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    for (Map.Entry<String, JsonElement> foundFairiesForLocationJson : foundFairiesForProfileJson.getValue().getAsJsonObject().asMap().entrySet()) {
                        Map<BlockPos, ProfileAwareWaypoint> fairiesForLocation = fairySouls.get(foundFairiesForLocationJson.getKey());
                        for (JsonElement foundFairy : foundFairiesForLocationJson.getValue().getAsJsonArray().asList()) {
                            fairiesForLocation.get(PosUtils.parsePosString(foundFairy.getAsString())).setFound(foundFairiesForProfileJson.getKey());
                        }
                    }
                }
                LOGGER.debug("[Skyblocker] Loaded found fairy souls");
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load found fairy souls", e);
            }
            LOGGER.info("[Skyblocker] Loaded {} fairy souls across {} locations", fairySouls.values().stream().mapToInt(Map::size).sum(), fairySouls.size());
        });
    }

    private static void saveFoundFairySouls(MinecraftClient client) {
        Map<String, Map<String, Set<BlockPos>>> foundFairies = new HashMap<>();
        for (Map.Entry<String, Map<BlockPos, ProfileAwareWaypoint>> fairiesForLocation : fairySouls.entrySet()) {
            for (ProfileAwareWaypoint fairySoul : fairiesForLocation.getValue().values()) {
                for (String profile : fairySoul.foundProfiles) {
                    foundFairies.computeIfAbsent(profile, profile_ -> new HashMap<>());
                    foundFairies.get(profile).computeIfAbsent(fairiesForLocation.getKey(), location_ -> new HashSet<>());
                    foundFairies.get(profile).get(fairiesForLocation.getKey()).add(fairySoul.pos);
                }
            }
        }

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
        HelperConfig.FairySouls fairySoulsConfig = SkyblockerConfigManager.get().helpers.fairySouls;

        if (fairySoulsConfig.enableFairySoulsHelper && fairySoulsLoaded.isDone() && fairySouls.containsKey(Utils.getLocationRaw())) {
            for (Waypoint fairySoul : fairySouls.get(Utils.getLocationRaw()).values()) {
                boolean fairySoulNotFound = fairySoul.shouldRender();
                if (!fairySoulsConfig.highlightFoundSouls && !fairySoulNotFound || fairySoulsConfig.highlightOnlyNearbySouls && fairySoul.pos.getSquaredDistance(context.camera().getPos()) > 2500) {
                    continue;
                }
                fairySoul.render(context);
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

        Map<BlockPos, ProfileAwareWaypoint> fairiesOnCurrentIsland = fairySouls.get(Utils.getLocationRaw());
        if (fairiesOnCurrentIsland == null) {
            LOGGER.warn("[Skyblocker] Failed to mark closest fairy soul as found because there are no fairy souls loaded on the current island. NEU repo probably failed to load.");
            return;
        }

        fairiesOnCurrentIsland.values().stream()
                .filter(Waypoint::shouldRender)
                .min(Comparator.comparingDouble(fairySoul -> fairySoul.pos.getSquaredDistance(player.getPos())))
                .filter(fairySoul -> fairySoul.pos.getSquaredDistance(player.getPos()) <= 16)
                .ifPresent(Waypoint::setFound);
    }

    public static void markAllFairiesOnCurrentIslandFound() {
        Map<BlockPos, ProfileAwareWaypoint> fairiesForLocation = fairySouls.get(Utils.getLocationRaw());
        if (fairiesForLocation != null) {
            fairiesForLocation.values().forEach(ProfileAwareWaypoint::setFound);
        }
    }

    public static void markAllFairiesOnCurrentIslandMissing() {
        Map<BlockPos, ProfileAwareWaypoint> fairiesForLocation = fairySouls.get(Utils.getLocationRaw());
        if (fairiesForLocation != null) {
            fairiesForLocation.values().forEach(ProfileAwareWaypoint::setMissing);
        }
    }
}
