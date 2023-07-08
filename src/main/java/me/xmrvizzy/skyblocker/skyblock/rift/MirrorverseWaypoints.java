package me.xmrvizzy.skyblocker.skyblock.rift;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

public class MirrorverseWaypoints {
	private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Identifier WAYPOINTS_JSON = new Identifier(SkyblockerMod.NAMESPACE, "mirrorverse_waypoints.json");
	private static final BlockPos[] LAVA_PATH_WAYPOINTS = new BlockPos[107];
	private static final BlockPos[] UPSIDE_DOWN_WAYPOINTS = new BlockPos[66];
	private static final BlockPos[] TURBULATOR_WAYPOINTS = new BlockPos[27];
	private static final float[] COLOR_COMPONENTS = DyeColor.RED.getColorComponents();

	static {
		loadWaypoints();
	}

	/**
	 * Loads the waypoint locations into memory
	 */
	private static void loadWaypoints() {
		try (BufferedReader reader = CLIENT.getResourceManager().openAsReader(WAYPOINTS_JSON)) {
			JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
			JsonArray sections = file.get("sections").getAsJsonArray();

			/// Lava Path
			JsonArray lavaPathWaypoints = sections.get(0).getAsJsonObject().get("waypoints").getAsJsonArray();

			for (int i = 0; i < lavaPathWaypoints.size(); i++) {
				JsonObject point = lavaPathWaypoints.get(i).getAsJsonObject();
				LAVA_PATH_WAYPOINTS[i] = new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt());
			}

			/// Upside Down Parkour
			JsonArray upsideDownParkourWaypoints = sections.get(1).getAsJsonObject().get("waypoints").getAsJsonArray();

			for (int i = 0; i < upsideDownParkourWaypoints.size(); i++) {
				JsonObject point = upsideDownParkourWaypoints.get(i).getAsJsonObject();
				UPSIDE_DOWN_WAYPOINTS[i] = new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt());
			}

			/// Turbulator Parkour
			JsonArray turbulatorParkourWaypoints = sections.get(2).getAsJsonObject().get("waypoints").getAsJsonArray();

			for (int i = 0; i < turbulatorParkourWaypoints.size(); i++) {
				JsonObject point = turbulatorParkourWaypoints.get(i).getAsJsonObject();
				TURBULATOR_WAYPOINTS[i] = new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt());
			}

		} catch (IOException e) {
			LOGGER.info("[Skyblocker] Mirrorverse Waypoints failed to load ;(");
			e.printStackTrace();
		}
	}

	protected static void render(WorldRenderContext wrc) {
		//I would also check for the mirrorverse location but the scoreboard stuff is not performant at all...
		if (Utils.isInTheRift() && SkyblockerConfig.get().locations.rift.mirrorverseWaypoints) {
			for (BlockPos pos : LAVA_PATH_WAYPOINTS) {
				RenderHelper.renderFilledIfVisible(wrc, pos, COLOR_COMPONENTS, 0.5f);
			}

			for (BlockPos pos : UPSIDE_DOWN_WAYPOINTS) {
				RenderHelper.renderFilledIfVisible(wrc, pos, COLOR_COMPONENTS, 0.5f);
			}

			for (BlockPos pos : TURBULATOR_WAYPOINTS) {
				RenderHelper.renderFilledIfVisible(wrc, pos, COLOR_COMPONENTS, 0.5f);
			}
		}
	}
}
