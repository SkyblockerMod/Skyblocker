package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.Tickable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.function.Supplier;

/**
 * A waypoint that does not render until it is seen.
 * It is the caller's responsibility to call {@link #tick(MinecraftClient)} to check if the waypoint is seen.
 */
public class SeenWaypoint extends Waypoint implements Tickable {
	private boolean seen = false;

	public SeenWaypoint(BlockPos pos, Type type, float[] colorComponents) {
		super(pos, type, colorComponents, false);
	}

	public SeenWaypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents) {
		super(pos, typeSupplier, colorComponents, false);
	}

	public boolean isSeen() {
		return seen;
	}

	@Override
	public Type getRenderType() {
		return seen ? super.getRenderType() : super.getRenderType().withoutBeacon();
	}

	@Override
	public boolean shouldRender() {
		return seen;
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!seen && isEnabled() && client.world != null && client.player != null /*&& FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1) */
				&& client.world.getChunkAsView(pos.getX() >> 4, pos.getZ() >> 4) != null) {
			BlockHitResult blockHitResult = client.world.raycast(new RaycastContext(client.player.getEyePos(), Vec3d.ofCenter(pos), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
			if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getBlockPos().equals(pos)) {
				seen = true;
			}
		}
	}
}
