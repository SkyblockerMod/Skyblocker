package de.hysky.skyblocker.skyblock.waypoint;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;

import java.util.*;

public class Waypoints {
    Codec<List<WaypointCategory>> CODEC = WaypointCategory.CODEC.listOf();
    private static final Map<String, WaypointCategory> waypoints = new HashMap<>();

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
}
