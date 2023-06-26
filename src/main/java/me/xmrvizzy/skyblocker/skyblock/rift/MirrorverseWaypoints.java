package me.xmrvizzy.skyblocker.skyblock.rift;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.x150.renderer.render.Renderer3d;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.mixin.accessor.FrustumInvoker;
import me.xmrvizzy.skyblocker.utils.FrustumUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MirrorverseWaypoints {
	private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Identifier WAYPOINTS_JSON = new Identifier("skyblocker", "mirrorverse_waypoints.json");
	private static final BlockPos[] LAVA_PATH_WAYPOINTS = new BlockPos[107];
	private static final BlockPos[] UPSIDE_DOWN_WAYPOINTS = new BlockPos[66];
	private static final BlockPos[] TURBULATOR_WAYPOINTS = new BlockPos[27];
	private static final float[] COLOR_COMPONENTS = DyeColor.RED.getColorComponents();
	private static final Vec3d ONE = new Vec3d(1, 1, 1);
	
	static {
		loadWaypoints();
	}
	
	/**
	 * Loads the waypoint locations into memory
	 */
	public static void loadWaypoints() {
		try(BufferedReader reader = CLIENT.getResourceManager().openAsReader(WAYPOINTS_JSON)) {
			JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
			JsonArray sections = file.get("sections").getAsJsonArray();
			
			/// Lava Path
			JsonArray lavaPathWaypoints = sections.get(0).getAsJsonObject().get("waypoints").getAsJsonArray();
			
			for(int i = 0; i < lavaPathWaypoints.size(); i++) {
				JsonObject point = lavaPathWaypoints.get(i).getAsJsonObject();
				LAVA_PATH_WAYPOINTS[i] = new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt());
			}
			
			/// Upside Down Parkour
			JsonArray upsideDownParkourWaypoints = sections.get(1).getAsJsonObject().get("waypoints").getAsJsonArray();
			
			for(int i = 0; i < upsideDownParkourWaypoints.size(); i++) {
				JsonObject point = upsideDownParkourWaypoints.get(i).getAsJsonObject();
				UPSIDE_DOWN_WAYPOINTS[i] = new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt());
			}
			
			/// Turbulator Parkour
			JsonArray turbulatorParkourWaypoints = sections.get(2).getAsJsonObject().get("waypoints").getAsJsonArray();
			
			for(int i = 0; i < turbulatorParkourWaypoints.size(); i++) {
				JsonObject point = turbulatorParkourWaypoints.get(i).getAsJsonObject();
				TURBULATOR_WAYPOINTS[i] = new BlockPos(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt());
			}
			
		} catch (IOException e) {
			LOGGER.info("[Skyblocker] Mirrorverse Waypoints failed to load ;(");
			e.printStackTrace();
		}
	}

	public static void render(WorldRenderContext wrc) {
		FrustumInvoker frustum = ((FrustumInvoker) FrustumUtils.getFrustum());
		//I would also check for the mirrorverse location but the scoreboard stuff is not performant at all...
		if(Utils.getLocationRaw().equals(TheRift.LOCATION) && SkyblockerConfig.get().locations.rift.mirrorverseWaypoints) {
			for(BlockPos pos : LAVA_PATH_WAYPOINTS) {
				if(frustum.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
					Renderer3d.renderFilled(wrc.matrixStack(), new Color(COLOR_COMPONENTS[0], COLOR_COMPONENTS[1], COLOR_COMPONENTS[2], 0.5f), Vec3d.of(pos), ONE);
				};
			}
			
			for(BlockPos pos : UPSIDE_DOWN_WAYPOINTS) {
				if(frustum.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
					Renderer3d.renderFilled(wrc.matrixStack(), new Color(COLOR_COMPONENTS[0], COLOR_COMPONENTS[1], COLOR_COMPONENTS[2], 0.5f), Vec3d.of(pos), ONE);
				};
			}
			
			for(BlockPos pos : TURBULATOR_WAYPOINTS) {
				if(frustum.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
					Renderer3d.renderFilled(wrc.matrixStack(), new Color(COLOR_COMPONENTS[0], COLOR_COMPONENTS[1], COLOR_COMPONENTS[2], 0.5f), Vec3d.of(pos), ONE);
				};
			}
		}
	}
}
