package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
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
    private static final Codec<List<WaypointGroup>> CODEC = WaypointGroup.CODEC.listOf();
    private static final Codec<List<WaypointGroup>> SKYTILS_CODEC = WaypointGroup.SKYTILS_CODEC.listOf();
    protected static final SystemToast.Type WAYPOINTS_TOAST_TYPE = new SystemToast.Type();

    private static final Path waypointsFile = FabricLoader.getInstance().getConfigDir().resolve(SkyblockerMod.NAMESPACE).resolve("waypoints.json");
    protected static final Multimap<String, WaypointGroup> waypoints = MultimapBuilder.hashKeys().arrayListValues().build();

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
            List<WaypointGroup> waypoints = CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(reader, JsonArray.class)).resultOrPartial(LOGGER::error).orElseThrow();
            waypoints.forEach(waypointGroup -> Waypoints.waypoints.put(waypointGroup.island(), waypointGroup));
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while loading waypoints", e);
        }
    }

    public static List<WaypointGroup> fromSkytils(String waypointsString, String defaultIsland) {
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

    public static List<WaypointGroup> fromSkytilsJson(String waypointGroupsString, String defaultIsland) {
        JsonArray waypointGroupsJson;
        try {
            waypointGroupsJson = SkyblockerMod.GSON.fromJson(waypointGroupsString, JsonObject.class).getAsJsonArray("categories");
        } catch (JsonSyntaxException e) {
            // Handle the case where there is only a single json list of waypoints and no group data.
            JsonObject waypointGroupJson = new JsonObject();
            waypointGroupJson.addProperty("name", "New Group");
            waypointGroupJson.addProperty("island", defaultIsland);
            waypointGroupJson.add("waypoints", SkyblockerMod.GSON.fromJson(waypointGroupsString, JsonArray.class));
            waypointGroupsJson = new JsonArray();
            waypointGroupsJson.add(waypointGroupJson);
        }
        List<WaypointGroup> waypointGroups = SKYTILS_CODEC.parse(JsonOps.INSTANCE, waypointGroupsJson).resultOrPartial(LOGGER::error).orElseThrow();
        return waypointGroups.stream().map(waypointGroup -> Location.from(waypointGroup.island()) == Location.UNKNOWN ? waypointGroup.withIsland(defaultIsland) : waypointGroup).toList();
    }

    public static String toSkytilsBase64(List<WaypointGroup> waypointGroups) {
        return Base64.getEncoder().encodeToString(toSkytilsJson(waypointGroups).getBytes());
    }

    public static String toSkytilsJson(List<WaypointGroup> waypointGroups) {
        JsonObject waypointGroupsJson = new JsonObject();
        waypointGroupsJson.add("categories", SKYTILS_CODEC.encodeStart(JsonOps.INSTANCE, waypointGroups).resultOrPartial(LOGGER::error).orElseThrow());
        return SkyblockerMod.GSON_COMPACT.toJson(waypointGroupsJson);
    }

    public static WaypointGroup fromColeweightJson(String waypointsJson, String defaultIsland) {
        return WaypointGroup.COLEWEIGHT_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(waypointsJson)).resultOrPartial(LOGGER::error).orElseThrow().withIsland(defaultIsland);
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

    public static Multimap<String, WaypointGroup> waypointsDeepCopy() {
        return waypoints.values().stream().map(WaypointGroup::deepCopy).collect(Multimaps.toMultimap(WaypointGroup::island, Function.identity(), () -> MultimapBuilder.hashKeys().arrayListValues().build()));
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().uiAndVisuals.waypoints.enableWaypoints) {
            Collection<WaypointGroup> groups = waypoints.get(Utils.getLocationRaw());
            for (WaypointGroup group : groups) {
                if (group != null) {
                    group.render(context);
                }
            }
        }
    }
}
