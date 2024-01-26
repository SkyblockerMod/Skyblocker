package de.hysky.skyblocker.utils.waypoint;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WaypointCategoryTest {
    @BeforeAll
    static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testCodecEncode() {
        WaypointCategory category = new WaypointCategory("category", "hub", List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "waypoint", new float[]{0f, 0f, 0f}, true)));
        Object categoryJson = WaypointCategory.CODEC.encodeStart(JsonOps.INSTANCE, category).result().orElseThrow();
        String expectedJson = "{\"name\":\"category\",\"island\":\"hub\",\"waypoints\":[{\"pos\":[0,0,0],\"name\":\"waypoint\",\"colorComponents\":[0.0,0.5,1.0],\"shouldRender\":false},{\"pos\":[-1,0,1],\"name\":\"waypoint\",\"colorComponents\":[0.0,0.0,0.0],\"shouldRender\":true}]}";

        Assertions.assertEquals(expectedJson, categoryJson.toString());
    }

    @Test
    void testCodecDecode() {
        String categoryJson = "{\"name\":\"category\",\"island\":\"hub\",\"waypoints\":[{\"pos\":[0,0,0],\"name\":\"waypoint\",\"colorComponents\":[0.0,0.5,1.0],\"shouldRender\":false},{\"pos\":[-1,0,1],\"name\":\"waypoint\",\"colorComponents\":[0.0,0.0,0.0],\"shouldRender\":true}]}";
        WaypointCategory category = WaypointCategory.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(categoryJson, JsonElement.class)).result().orElseThrow();
        WaypointCategory expectedCategory = new WaypointCategory("category", "hub", List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "waypoint", new float[]{0f, 0f, 0f}, true)));

        Assertions.assertEquals(expectedCategory, category);
    }
}
