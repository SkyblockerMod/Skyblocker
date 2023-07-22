package me.xmrvizzy.skyblocker.utils.render.culling;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.cache.ArrayOcclusionCache;
import com.logisticscraft.occlusionculling.util.Vec3d;
import me.xmrvizzy.skyblocker.utils.render.FrustumUtils;
import net.minecraft.client.MinecraftClient;

public class OcclusionCulling {
	private static final int TRACING_DISTANCE = 128;
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static OcclusionCullingInstance instance = null;

	// Reused objects to reduce allocation overhead
	private static final Vec3d cameraPos = new Vec3d(0, 0, 0);
	private static final Vec3d min = new Vec3d(0, 0, 0);
	private static final Vec3d max = new Vec3d(0, 0, 0);

	/**
	 * Initializes the occlusion culling instance
	 */
	public static void init() {
		instance = new OcclusionCullingInstance(TRACING_DISTANCE, new WorldProvider(), new ArrayOcclusionCache(TRACING_DISTANCE), 2);
	}

	private static void updateCameraPos() {
		var camera = CLIENT.gameRenderer.getCamera().getPos();
		cameraPos.set(camera.x, camera.y, camera.z);
	}

	/**
	 * This first checks checks if the bounding box is within the camera's FOV, if
	 * it is then it checks for whether it's occluded or not.
	 * 
	 * @return A boolean representing whether the bounding box is fully visible or
	 *         not.
	 */
	public static boolean isVisible(double x1, double y1, double z1, double x2, double y2, double z2) {
		if (!FrustumUtils.isVisible(x1, y1, z1, x2, y2, z2)) return false;

		updateCameraPos();
		min.set(x1, y1, z1);
		max.set(x2, y2, z2);

		return instance.isAABBVisible(min, max, cameraPos);
	}
}
