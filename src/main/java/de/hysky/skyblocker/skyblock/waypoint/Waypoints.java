package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Waypoints {
    public static final Logger LOGGER = LoggerFactory.getLogger(Waypoints.class);
    private static final Codec<List<WaypointCategory>> CODEC = WaypointCategory.CODEC.listOf();
    private static final Codec<List<WaypointCategory>> SKYTILS_CODEC = WaypointCategory.SKYTILS_CODEC.listOf();
    protected static final SystemToast.Type WAYPOINTS_TOAST_TYPE = new SystemToast.Type();

    private static final Path waypointsFile = FabricLoader.getInstance().getConfigDir().resolve(SkyblockerMod.NAMESPACE).resolve("waypoints.json");
    protected static final Multimap<String, WaypointCategory> waypoints = MultimapBuilder.hashKeys().arrayListValues().build();

    @Init
    public static void init() {
        loadWaypoints();
        ClientLifecycleEvents.CLIENT_STOPPING.register(Waypoints::saveWaypoints);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Waypoints::render);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("waypoints").executes(Scheduler.queueOpenScreenCommand(() -> new WaypointsScreen(MinecraftClient.getInstance().currentScreen))))));
    }

    public static void loadWaypoints() {
        waypoints.clear();
        try (BufferedReader reader = Files.newBufferedReader(waypointsFile)) {
            List<WaypointCategory> waypoints = CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(reader, JsonArray.class)).resultOrPartial(LOGGER::error).orElseThrow();
            waypoints.forEach(waypointCategory -> Waypoints.waypoints.put(waypointCategory.island(), waypointCategory));
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while loading waypoints", e);
        }
    }

    public static List<WaypointCategory> fromSkytils(String waypointsString, String defaultIsland) {
        try {
            if (waypointsString.startsWith("<Skytils-Waypoint-Data>(V")) {
                int version = Integer.parseInt(waypointsString.substring(25, waypointsString.indexOf(')')));
                waypointsString = waypointsString.substring(waypointsString.indexOf(':') + 1);
                if (version == 1) {
                    try (GZIPInputStream reader = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(waypointsString)))) {
                        return fromSkytilsJson(IOUtils.toString(reader, StandardCharsets.UTF_8), defaultIsland);
                    }
                } else {
                    LOGGER.error("[Skyblocker Waypoints] Unknown Skytils waypoint data version: {}", version);
                }
            } else return fromSkytilsJson(new String(Base64.getDecoder().decode(waypointsString)), defaultIsland);
        } catch (NumberFormatException e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skytils waypoint data version", e);
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while decoding Skytils waypoint data", e);
        }
        return Collections.emptyList();
    }

    public static List<WaypointCategory> fromSkytilsJson(String waypointCategoriesString, String defaultIsland) {
        JsonArray waypointCategoriesJson;
        try {
            waypointCategoriesJson = SkyblockerMod.GSON.fromJson(waypointCategoriesString, JsonObject.class).getAsJsonArray("categories");
        } catch (JsonSyntaxException e) {
            // Handle the case where there is only a single json list of waypoints and no category data.
            JsonObject waypointCategoryJson = new JsonObject();
            waypointCategoryJson.addProperty("name", "New Category");
            waypointCategoryJson.addProperty("island", defaultIsland);
            waypointCategoryJson.add("waypoints", SkyblockerMod.GSON.fromJson(waypointCategoriesString, JsonArray.class));
            waypointCategoriesJson = new JsonArray();
            waypointCategoriesJson.add(waypointCategoryJson);
        }
        List<WaypointCategory> waypointCategories = SKYTILS_CODEC.parse(JsonOps.INSTANCE, waypointCategoriesJson).resultOrPartial(LOGGER::error).orElseThrow();
        return waypointCategories.stream().map(waypointCategory -> Location.from(waypointCategory.island()) == Location.UNKNOWN ? waypointCategory.withIsland(defaultIsland) : waypointCategory).toList();
    }

    public static String toSkytilsBase64(List<WaypointCategory> waypointCategories) {
        return Base64.getEncoder().encodeToString(toSkytilsJson(waypointCategories).getBytes());
    }

    public static String toSkytilsJson(List<WaypointCategory> waypointCategories) {
        JsonObject waypointCategoriesJson = new JsonObject();
        waypointCategoriesJson.add("categories", SKYTILS_CODEC.encodeStart(JsonOps.INSTANCE, waypointCategories).resultOrPartial(LOGGER::error).orElseThrow());
        return SkyblockerMod.GSON_COMPACT.toJson(waypointCategoriesJson);
    }

    public static void saveWaypoints(MinecraftClient client) {
        try (BufferedWriter writer = Files.newBufferedWriter(waypointsFile)) {
            JsonElement waypointsJson = CODEC.encodeStart(JsonOps.INSTANCE, List.copyOf(waypoints.values())).resultOrPartial(LOGGER::error).orElseThrow();
            SkyblockerMod.GSON.toJson(waypointsJson, writer);
            LOGGER.info("[Skyblocker Waypoints] Saved waypoints");
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while saving waypoints", e);
        }
    }

    public static Multimap<String, WaypointCategory> waypointsDeepCopy() {
        return waypoints.values().stream().map(WaypointCategory::deepCopy).collect(Multimaps.toMultimap(WaypointCategory::island, Function.identity(), () -> MultimapBuilder.hashKeys().arrayListValues().build()));
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().uiAndVisuals.waypoints.enableWaypoints) {
            Collection<WaypointCategory> categories = waypoints.get(Utils.getLocationRaw());
            for (WaypointCategory category : categories) {
                if (category != null) {
                    category.render(context);
                }
            }
        }
    }
}
