package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SecretWaypointTest {
    private final Gson gson = new Gson();

    @Test
    void testCodecSerialize() {
        SecretWaypoint waypoint = new SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos.ORIGIN);
        JsonElement json = SecretWaypoint.CODEC.encodeStart(JsonOps.INSTANCE, waypoint).result().orElseThrow();
        String expectedJson = "{\"secretIndex\":0,\"category\":\"default\",\"name\":{\"text\":\"name\"},\"pos\":[0,0,0]}";

        Assertions.assertEquals(expectedJson, json.toString());
    }

    @Test
    void testCodecDeserialize() {
        String json = "{\"secretIndex\":0,\"category\":\"default\",\"name\":{\"text\":\"name\"},\"pos\":[0,0,0]}";
        SecretWaypoint waypoint = SecretWaypoint.CODEC.parse(JsonOps.INSTANCE, gson.fromJson(json, JsonElement.class)).result().orElseThrow();
        SecretWaypoint expectedWaypoint = new SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos.ORIGIN);

        equal(expectedWaypoint, waypoint);
    }

    @Test
    void testListCodecSerialize() {
        List<SecretWaypoint> waypoints = List.of(new SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos.ORIGIN), new SecretWaypoint(1, SecretWaypoint.Category.CHEST, "name", new BlockPos(-1, 0, 1)));
        JsonElement json = SecretWaypoint.LIST_CODEC.encodeStart(JsonOps.INSTANCE, waypoints).result().orElseThrow();
        String expectedJson = "[{\"secretIndex\":0,\"category\":\"default\",\"name\":{\"text\":\"name\"},\"pos\":[0,0,0]},{\"secretIndex\":1,\"category\":\"chest\",\"name\":{\"text\":\"name\"},\"pos\":[-1,0,1]}]";

        Assertions.assertEquals(expectedJson, json.toString());
    }

    @Test
    void testListCodecDeserialize() {
        String json = "[{\"secretIndex\":0,\"category\":\"default\",\"name\":{\"text\":\"name\"},\"pos\":[0,0,0]},{\"secretIndex\":1,\"category\":\"chest\",\"name\":{\"text\":\"name\"},\"pos\":[-1,0,1]}]";
        List<SecretWaypoint> waypoints = SecretWaypoint.LIST_CODEC.parse(JsonOps.INSTANCE, gson.fromJson(json, JsonElement.class)).result().orElseThrow();
        List<SecretWaypoint> expectedWaypoints = List.of(new SecretWaypoint(0, SecretWaypoint.Category.DEFAULT, "name", BlockPos.ORIGIN), new SecretWaypoint(1, SecretWaypoint.Category.CHEST, "name", new BlockPos(-1, 0, 1)));

        Assertions.assertEquals(expectedWaypoints.size(), waypoints.size());
        for (int i = 0; i < expectedWaypoints.size(); i++) {
            SecretWaypoint expectedWaypoint = expectedWaypoints.get(i);
            SecretWaypoint waypoint = waypoints.get(i);
            equal(expectedWaypoint, waypoint);
        }
    }

    @Test
    void testGetCategory() {
        JsonObject waypointJson = new JsonObject();
        waypointJson.addProperty("category", "chest");
        SecretWaypoint.Category category = SecretWaypoint.Category.get(waypointJson);
        Assertions.assertEquals(SecretWaypoint.Category.CHEST, category);
    }

    @Test
    void testGetCategoryDefault() {
        JsonObject waypointJson = new JsonObject();
        waypointJson.addProperty("category", "");
        SecretWaypoint.Category category = SecretWaypoint.Category.get(waypointJson);
        Assertions.assertEquals(SecretWaypoint.Category.DEFAULT, category);
    }

    private static void equal(SecretWaypoint expectedWaypoint, SecretWaypoint waypoint) {
        Assertions.assertEquals(expectedWaypoint.secretIndex, waypoint.secretIndex);
        Assertions.assertEquals(expectedWaypoint.category, waypoint.category);
        Assertions.assertEquals(expectedWaypoint.name, waypoint.name);
        Assertions.assertEquals(expectedWaypoint.pos, waypoint.pos);
    }
}
