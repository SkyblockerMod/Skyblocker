package de.hysky.skyblocker.skyblock.rift;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class MirrorverseWaypoints {
	private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");
	private static final Supplier<Waypoint.Type> WAYPOINT_TYPE = () -> Waypoint.Type.HIGHLIGHT;
	private static final Identifier WAYPOINTS_JSON = SkyblockerMod.id("rift/mirrorverse_waypoints.json");
	private static Waypoint[] LAVA_PATH_WAYPOINTS;
	private static Waypoint[] UPSIDE_DOWN_WAYPOINTS;
	private static Waypoint[] TURBULATOR_WAYPOINTS;
	private static final float[] COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);

	private static CompletableFuture<Void> waypointsLoaded;

	/**
	 * Loads the waypoint locations into memory
	 */
	static void load(Minecraft client) {
		waypointsLoaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(WAYPOINTS_JSON)) {
				JsonArray sections = JsonParser.parseReader(reader).getAsJsonObject().get("sections").getAsJsonArray();

				/// Lava Path
				LAVA_PATH_WAYPOINTS = loadWaypoints(sections.get(0).getAsJsonObject().get("waypoints").getAsJsonArray());

				/// Upside Down Parkour
				UPSIDE_DOWN_WAYPOINTS = loadWaypoints(sections.get(1).getAsJsonObject().get("waypoints").getAsJsonArray());

				/// Turbulator Parkour
				TURBULATOR_WAYPOINTS = loadWaypoints(sections.get(2).getAsJsonObject().get("waypoints").getAsJsonArray());
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Mirrorverse Waypoints failed to load ;(", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static Waypoint[] loadWaypoints(JsonArray waypointsJson) {
		Waypoint[] waypoints = new Waypoint[waypointsJson.size()];
		for (int i = 0; i < waypointsJson.size(); i++) {
			JsonObject point = waypointsJson.get(i).getAsJsonObject();
			waypoints[i] = new Waypoint(new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt()), WAYPOINT_TYPE, COLOR_COMPONENTS, false);
		}
		return waypoints;
	}

	protected static void extractRendering(PrimitiveCollector collector) {
		if (Utils.isInTheRift() && Utils.getArea() == Area.TheRift.MIRRORVERSE && SkyblockerConfigManager.get().otherLocations.rift.mirrorverseWaypoints && waypointsLoaded.isDone()) {
			for (Waypoint waypoint : LAVA_PATH_WAYPOINTS) {
				waypoint.extractRendering(collector);
			}

			for (Waypoint waypoint : UPSIDE_DOWN_WAYPOINTS) {
				waypoint.extractRendering(collector);
			}

			for (Waypoint waypoint : TURBULATOR_WAYPOINTS) {
				waypoint.extractRendering(collector);
			}
		}
	}
}
