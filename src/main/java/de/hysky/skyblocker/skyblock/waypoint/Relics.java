package de.hysky.skyblocker.skyblock.waypoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.OtherLocationsConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Relics {
    private static final Logger LOGGER = LoggerFactory.getLogger(Relics.class);
    private static final Supplier<Waypoint.Type> TYPE_SUPPLIER = () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType;
    private static CompletableFuture<Void> relicsLoaded;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static int totalRelics = 0;
    private static final Map<BlockPos, ProfileAwareWaypoint> relics = new HashMap<>();

    @Init
    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(Relics::loadRelics);
        ClientLifecycleEvents.CLIENT_STOPPING.register(Relics::saveFoundRelics);
        ClientCommandRegistrationCallback.EVENT.register(Relics::registerCommands);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Relics::render);
        ClientReceiveMessageEvents.GAME.register(Relics::onChatMessage);
    }

    private static void loadRelics(MinecraftClient client) {
        relicsLoaded = CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = client.getResourceManager().openAsReader(Identifier.of(SkyblockerMod.NAMESPACE, "spidersden/relics.json"))) {
                for (Map.Entry<String, JsonElement> json : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    if (json.getKey().equals("total")) {
                        totalRelics = json.getValue().getAsInt();
                    } else if (json.getKey().equals("locations")) {
                        for (JsonElement locationJson : json.getValue().getAsJsonArray().asList()) {
                            JsonObject posData = locationJson.getAsJsonObject();
                            BlockPos pos = new BlockPos(posData.get("x").getAsInt(), posData.get("y").getAsInt(), posData.get("z").getAsInt());
                            relics.put(pos, new ProfileAwareWaypoint(pos, TYPE_SUPPLIER, ColorUtils.getFloatComponents(DyeColor.YELLOW), ColorUtils.getFloatComponents(DyeColor.BROWN)));
                        }
                    }
                }
                LOGGER.info("[Skyblocker] Loaded relics locations");
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load relics locations", e);
            }

            try (BufferedReader reader = Files.newBufferedReader(SkyblockerMod.CONFIG_DIR.resolve("found_relics.json"))) {
                for (Map.Entry<String, JsonElement> profileJson : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    for (JsonElement foundRelicsJson : profileJson.getValue().getAsJsonArray().asList()) {
                        relics.get(PosUtils.parsePosString(foundRelicsJson.getAsString())).setFound(profileJson.getKey());
                    }
                }
                LOGGER.debug("[Skyblocker] Loaded found relics");
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load found relics", e);
            }
        });
    }

    private static void saveFoundRelics(MinecraftClient client) {
        Map<String, Set<BlockPos>> foundRelics = new HashMap<>();
        for (ProfileAwareWaypoint relic : relics.values()) {
            for (String profile : relic.foundProfiles) {
                foundRelics.computeIfAbsent(profile, profile_ -> new HashSet<>());
                foundRelics.get(profile).add(relic.pos);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(SkyblockerMod.CONFIG_DIR.resolve("found_relics.json"))) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, Set<BlockPos>> foundRelicsForProfile : foundRelics.entrySet()) {
                JsonArray foundRelicsJson = new JsonArray();
                for (BlockPos foundRelic : foundRelicsForProfile.getValue()) {
                    foundRelicsJson.add(PosUtils.getPosString(foundRelic));
                }
                json.add(foundRelicsForProfile.getKey(), foundRelicsJson);
            }
            SkyblockerMod.GSON.toJson(json, writer);
            LOGGER.debug("[Skyblocker] Saved found relics");
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to write found relics to file", e);
        }
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("relics")
                        .then(literal("markAllFound").executes(context -> {
                            relics.values().forEach(ProfileAwareWaypoint::setFound);
                            context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.relics.markAllFound")));
                            return 1;
                        }))
                        .then(literal("markAllMissing").executes(context -> {
                            relics.values().forEach(ProfileAwareWaypoint::setMissing);
                            context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.relics.markAllMissing")));
                            return 1;
                        }))));
    }

    private static void render(WorldRenderContext context) {
        OtherLocationsConfig.Relics config = SkyblockerConfigManager.get().otherLocations.spidersDen.relics;

        if (config.enableRelicsHelper && relicsLoaded.isDone() && Utils.getLocationRaw().equals("combat_1")) {
            for (ProfileAwareWaypoint relic : relics.values()) {
                boolean isRelicMissing = relic.shouldRender();
                if (!isRelicMissing && !config.highlightFoundRelics) continue;
                relic.render(context);
            }
        }
    }

    private static void onChatMessage(Text text, boolean overlay) {
        String message = text.getString();
        if (message.equals("You've already found this relic!") || message.startsWith("+10,000 Coins! (") && message.endsWith("/28 Relics)")) {
            markClosestRelicFound();
        }
    }

    private static void markClosestRelicFound() {
        if (!relicsLoaded.isDone()) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            LOGGER.warn("[Skyblocker] Failed to mark closest relic as found because player is null");
            return;
        }
        relics.values().stream()
                .filter(Waypoint::shouldRender)
                .min(Comparator.comparingDouble(relic -> relic.pos.getSquaredDistance(player.getPos())))
                .filter(relic -> relic.pos.getSquaredDistance(player.getPos()) <= 16)
                .ifPresent(Waypoint::setFound);
    }
}
