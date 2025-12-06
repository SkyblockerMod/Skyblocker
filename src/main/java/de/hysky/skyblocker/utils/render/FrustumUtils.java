package de.hysky.skyblocker.utils.render;

import org.joml.FrustumIntersection;

import de.hysky.skyblocker.mixins.accessors.FrustumInvoker;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;

public class FrustumUtils {

	public static boolean isVisible(Frustum frustum, Box box) {
		return isVisible(frustum, box);
	}

	public static boolean isVisible(Frustum frustum, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int plane = ((FrustumInvoker) frustum).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ);

		return plane == FrustumIntersection.INSIDE || plane == FrustumIntersection.INTERSECT;
	}
}
