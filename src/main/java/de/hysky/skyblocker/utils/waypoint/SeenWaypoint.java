package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.FrustumUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.function.Supplier;

public class SeenWaypoint extends Waypoint implements Tickable {
	private boolean seen = false;

	public SeenWaypoint(BlockPos pos, Type type, float[] colorComponents) {
		super(pos, type, colorComponents);
	}

	public SeenWaypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents) {
		super(pos, typeSupplier, colorComponents);
	}

	public boolean isSeen() {
		return seen;
	}

	@Override
	public Type getRenderType() {
		return seen ? super.getRenderType() : super.getRenderType().withoutBeacon();
	}

	@Override
	public boolean shouldRenderThroughWalls() {
		return seen;
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!seen && shouldRender() && client.world != null && client.player != null && FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1) && client.world.raycast(new RaycastContext(client.player.getEyePos(), Vec3d.ofCenter(pos), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player)).getType() == HitResult.Type.MISS) {
			seen = true;
		}
	}
}
