package de.hysky.skyblocker.utils;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Boxes {
	/**
	 * Returns the vector of the min pos of this box.
	 * @deprecated Use {@link AABB#getMinPosition()} instead.
	 */
	@Deprecated(since = "1.22")
	public static Vec3 getMinVec(AABB box) {
		return box.getMinPosition();
	}

	/**
	 * Returns the vector of the max pos of this box.
	 * @deprecated Use {@link AABB#getMaxPosition()} instead.
	 */
	@Deprecated(since = "1.22")
	public static Vec3 getMaxVec(AABB box) {
		return box.getMaxPosition();
	}

	/** Returns the vector of the side lengths of this box. **/
	public static Vec3 getLengthVec(AABB box) {
		return new Vec3(box.getXsize(), box.getYsize(), box.getZsize());
	}

	/** Offsets this box so that minX, minY and minZ are all zero. **/
	public static AABB moveToZero(AABB box) {
		return box.move(box.getMinPosition().reverse());
	}

	/** Returns the distance between to oppisite corners of the box. **/
	public static double getCornerLength(AABB box) {
		return box.getMinPosition().distanceTo(box.getMaxPosition());
	}

	/** Returns the length of an axis in the box. **/
	public static double getAxisLength(AABB box, Axis axis) {
		return box.max(axis) - box.min(axis);
	}

	/** Returns a box with each axis multiplied by the amount specified. **/
	public static AABB multiply(AABB box, double amount) {
		return multiply(box, amount, amount, amount);
	}

	/** Returns a box with each axis multiplied by the amount specified. **/
	public static AABB multiply(AABB box, double x, double y, double z) {
		return box.inflate(
				getAxisLength(box, Axis.X) * (x - 1) / 2d,
				getAxisLength(box, Axis.Y) * (y - 1) / 2d,
				getAxisLength(box, Axis.Z) * (z - 1) / 2d);
	}

	public static AABB lerpEntityBoundingBox(Entity entity, float partialTick) {
		// These names are so incredibly bad. Why is the function that returns the lerped
		// position called "getPosition" when the function that returns the non-lerped position
		// is just called "position"? Find me a single person that can tell me how the two
		// functions differ just by reading their names. A real human being sat down and picked
		// these names. Seriously.

		// Compute how much more the entity has to move backwards from its current position to reach its interpolated position
		// (old - new) * (1 - partialTick)

		// faster than recalculating a hitbox from the entity's current pose. very balzingaly fast
		final float remainingTick = 1 - partialTick;
		final Vec3 backwardsDeltaMovement = entity.oldPosition()
				.subtract(entity.position())
				.multiply(remainingTick, remainingTick, remainingTick);
		// Subtract this vector from the entity hitbox
		return entity.getBoundingBox().move(backwardsDeltaMovement);
	}
}
