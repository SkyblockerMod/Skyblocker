package de.hysky.skyblocker.utils.render;

import org.joml.FrustumIntersection;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import de.hysky.skyblocker.mixins.accessors.FrustumInvoker;

public class FrustumUtils {

	public static boolean isVisible(Frustum frustum, AABB box) {
		return isVisible(frustum, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	public static boolean isVisible(Frustum frustum, BlockPos pos) {
		return isVisible(frustum, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public static boolean isVisible(Frustum frustum, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		int plane = ((FrustumInvoker) frustum).invokeCubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ);

		return plane == FrustumIntersection.INSIDE || plane == FrustumIntersection.INTERSECT;
	}
}
