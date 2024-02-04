package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public class Waypoints {
    private static final Logger LOGGER = LoggerFactory.getLogger(Waypoints.class);
    private static final Codec<List<WaypointCategory>> CODEC = WaypointCategory.CODEC.listOf();
    private static final Path waypointsFile = FabricLoader.getInstance().getConfigDir().resolve(SkyblockerMod.NAMESPACE).resolve("waypoints.json");
    static final Multimap<String, WaypointCategory> waypoints = MultimapBuilder.hashKeys().arrayListValues().build();

    public static void init() {
        loadWaypoints();
        ClientLifecycleEvents.CLIENT_STOPPING.register(Waypoints::saveWaypoints);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Waypoints::render);
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

    public static Collection<WaypointCategory> fromSkytilsBase64(String base64) {
        return fromSkytilsJson(new String(Base64.getDecoder().decode(base64)));
    }

    public static Collection<WaypointCategory> fromSkytilsJson(String waypointCategories) {
        JsonObject waypointCategoriesJson = SkyblockerMod.GSON.fromJson(waypointCategories, JsonObject.class);
        return waypointCategoriesJson.getAsJsonArray("categories").asList().stream()
                .map(JsonObject.class::cast)
                .map(WaypointCategory::fromSkytilsJson)
                .toList();
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

    public static void render(WorldRenderContext context) {
        Collection<WaypointCategory> categories = waypoints.get(Utils.getLocationRaw());
        for (WaypointCategory category : categories) {
            if (category != null) {
                category.render(context);
            }
        }
    }
}
