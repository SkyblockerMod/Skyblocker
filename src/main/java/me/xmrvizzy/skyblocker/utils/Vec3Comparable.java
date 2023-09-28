/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 * Copyright (C) 2023 Skyblocker contributors
 *
 * This file is part of Skyblocker.
 *
 * The majority of this code is taken from NotEnoughUpdates,
 * slightly adjusted to port to Fabric and 1.20.x.
 *
 * Skyblocker is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Skyblocker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Skyblocker. If not, see <https://www.gnu.org/licenses/>.
 */

package me.xmrvizzy.skyblocker.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Vec3Comparable extends Vec3d implements Comparable<Vec3Comparable> {
    public static final Vec3Comparable NULL_VECTOR = new Vec3Comparable(0, 0, 0);

    public Vec3Comparable(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3Comparable(Vec3i source) {
        super(source.getX(), source.getY(), source.getZ());
    }

    public Vec3Comparable(Vec3d source) {
        super(source.getX(), source.getY(), source.getZ());
    }

    public Vec3Comparable(BlockPos source) {
        super(source.getX(), source.getY(), source.getZ());
    }

    public Vec3Comparable(Vec3Comparable source) {
        super(source.getX(), source.getY(), source.getZ());
    }

    @Override
    public Vec3Comparable normalize() {
        return new Vec3Comparable(super.normalize());
    }

    @Override
    public Vec3Comparable crossProduct(Vec3d vec) {
        return new Vec3Comparable(super.crossProduct(vec));
    }

    @Override
    public Vec3Comparable subtract(Vec3d vec) {
        return new Vec3Comparable(super.subtract(vec));
    }

    @Override
    public Vec3Comparable subtract(double x, double y, double z) {
        return new Vec3Comparable(super.subtract(x, y, z));
    }

    @Override
    public Vec3Comparable add(Vec3d other) {
        return new Vec3Comparable(super.add(other));
    }

    @Override
    public Vec3Comparable add(double x, double y, double z) {
        return new Vec3Comparable(super.add(x, y, z));
    }

    @Override
    public Vec3Comparable rotateX(float x) {
        return new Vec3Comparable(super.rotateX(x));
    }

    @Override
    public Vec3Comparable rotateY(float y) {
        return new Vec3Comparable(super.rotateY(y));
    }

    @Override
    public Vec3Comparable rotateZ(float z) {
        return new Vec3Comparable(super.rotateZ(z));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Vec3Comparable)) {
            return false;
        } else {
            Vec3Comparable vec3c = (Vec3Comparable) other;
            return this.getX() == vec3c.getX() && this.getY() == vec3c.getY() && this.getZ() == vec3c.getZ();
        }
    }

    @Override
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + doubleToLongBits(getX());
        bits = 31L * bits + doubleToLongBits(getY());
        bits = 31L * bits + doubleToLongBits(getZ());
        return (int) (bits ^ (bits >> 32));
    }

    public int compareTo(Vec3Comparable other) {
        return this.getY() == other.getY() ?
            (this.getZ() == other.getZ() ?
                (int) (this.getX() - other.getX())
                : (int) (this.getZ() - other.getZ()))
            : (int) (this.getY() - other.getY());
    }

    public boolean signumEquals(Vec3d other) {
        return Math.signum(getX()) == Math.signum(other.getX()) &&
            Math.signum(getY()) == Math.signum(other.getY()) &&
            Math.signum(getZ()) == Math.signum(other.getZ());
    }

    private static long doubleToLongBits(double d) {
        return d == 0.0 ? 0L : Double.doubleToLongBits(d);
    }
}
