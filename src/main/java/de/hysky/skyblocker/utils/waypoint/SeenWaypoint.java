package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.Tickable;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A waypoint that does not render until it is seen.
 * It is the caller's responsibility to call {@link #tick(Minecraft)} to check if the waypoint is seen.
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
	public void tick(Minecraft client) {
		if (!seen && isEnabled() && client.level != null && client.player != null /*&& FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1) */
				&& client.level.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4) != null) {
			BlockHitResult blockHitResult = client.level.clip(new ClipContext(client.player.getEyePosition(), Vec3.atCenterOf(pos), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, client.player));
			if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getBlockPos().equals(pos)) {
				seen = true;
			}
		}
	}
}
