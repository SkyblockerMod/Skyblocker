package de.hysky.skyblocker.utils.waypoint;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WaypointGroupTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testCodecEncode() {
		WaypointGroup group = new WaypointGroup("group", Location.HUB, List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "waypoint", new float[]{0f, 0f, 0f}, true)));
		JsonElement groupJson = WaypointGroup.CODEC.encodeStart(JsonOps.INSTANCE, group).result().orElseThrow();
		JsonElement expectedJson = SkyblockerMod.GSON.fromJson("{\"name\":\"group\",\"island\":\"hub\",\"waypoints\":[{\"colorComponents\":[0.0,0.5,1.0],\"alpha\":0.5,\"shouldRender\":false,\"pos\":[0,0,0],\"name\":\"waypoint\"},{\"colorComponents\":[0.0,0.0,0.0],\"alpha\":0.5,\"shouldRender\":true,\"pos\":[-1,0,1],\"name\":\"waypoint\"}]}", JsonElement.class);

		Assertions.assertEquals(expectedJson, groupJson);
	}

	@Test
	void testCodecDecode() {
		String groupJson = "{\"name\":\"group\",\"island\":\"hub\",\"waypoints\":[{\"colorComponents\":[0.0,0.5,1.0],\"alpha\":0.5,\"shouldRender\":false,\"pos\":[0,0,0],\"name\":\"waypoint\"},{\"colorComponents\":[0.0,0.0,0.0],\"alpha\":0.5,\"shouldRender\":true,\"pos\":[-1,0,1],\"name\":\"waypoint\"}]}";
		WaypointGroup group = WaypointGroup.CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(groupJson, JsonElement.class)).result().orElseThrow();
		WaypointGroup expectedGroup = new WaypointGroup("group", Location.HUB, List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "waypoint", new float[]{0f, 0f, 0f}, true)));

		Assertions.assertEquals(expectedGroup, group);
	}
}
