package de.hysky.skyblocker.utils;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class Boxes {
    /**
     * Returns the vector of the min pos of this box.
     * @deprecated Use {@link Box#getMinPos()} instead.
     */
    @Deprecated(since = "1.22")
    public static Vec3d getMinVec(Box box) {
        return box.getMinPos();
    }

    /**
     * Returns the vector of the max pos of this box.
     * @deprecated Use {@link Box#getMaxPos()} instead.
     */
    @Deprecated(since = "1.22")
    public static Vec3d getMaxVec(Box box) {
        return box.getMaxPos();
    }

    /** Returns the vector of the side lengths of this box. **/
    public static Vec3d getLengthVec(Box box) {
        return new Vec3d(box.getLengthX(), box.getLengthY(), box.getLengthZ());
    }

    /** Offsets this box so that minX, minY and minZ are all zero. **/
    public static Box moveToZero(Box box) {
        return box.offset(box.getMinPos().negate());
    }

    /** Returns the distance between to oppisite corners of the box. **/
    public static double getCornerLength(Box box) {
        return box.getMinPos().distanceTo(box.getMaxPos());
    }

    /** Returns the length of an axis in the box. **/
    public static double getAxisLength(Box box, Axis axis) {
        return box.getMax(axis) - box.getMin(axis);
    }

    /** Returns a box with each axis multiplied by the amount specified. **/
    public static Box multiply(Box box, double amount) {
        return multiply(box, amount, amount, amount);
    }

    /** Returns a box with each axis multiplied by the amount specified. **/
    public static Box multiply(Box box, double x, double y, double z) {
        return box.expand(
                getAxisLength(box, Axis.X) * (x - 1) / 2d,
                getAxisLength(box, Axis.Y) * (y - 1) / 2d,
                getAxisLength(box, Axis.Z) * (z - 1) / 2d);
    }
}
