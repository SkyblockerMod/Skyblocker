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

package de.hysky.skyblocker.utils;

import net.minecraft.util.math.Vec3d;

/**
 * Represents a line using two points along the line or a segment with endpoints.
 */
public class Line {
    private static final double DOUBLE_EPSILON = 4.94065645841247E-324;
    public Vec3d point1;
    public Vec3d point2;

    public Line(Vec3d first, Vec3d second) {
        point1 = first;
        point2 = second;
    }

    public Vec3d getMidpoint() {
        return new Vec3d(
            (point1.getX() + point2.getX()) / 2.0,
            (point1.getY() + point2.getY()) / 2.0,
            (point1.getZ() + point2.getZ()) / 2.0
        );
    }

    /**
     * Calculates the intersection line segment between 2 lines
     * Based on http://paulbourke.net/geometry/pointlineplane/calclineline.cs
     *
     * @return The intersection {@link Line} or {@code null} if no solution found
     */
    public Line getIntersectionLineSegment(Line other) {
        Vec3d p1 = this.point1;
        Vec3d p2 = this.point2;
        Vec3d p3 = other.point1;
        Vec3d p4 = other.point2;
        Vec3d p13 = p1.subtract(p3);
        Vec3d p43 = p4.subtract(p3);

        if (lengthSquared(p43) < DOUBLE_EPSILON) {
            return null;
        }

        Vec3d p21 = p2.subtract(p1);
        if (lengthSquared(p21) < DOUBLE_EPSILON) {
            return null;
        }

        double d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
        double d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
        double d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
        double d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
        double d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();

        double denom = d2121 * d4343 - d4321 * d4321;
        if (Math.abs(denom) < DOUBLE_EPSILON) {
            return null;
        }
        double numer = d1343 * d4321 - d1321 * d4343;

        double mua = numer / denom;
        double mub = (d1343 + d4321 * (mua)) / d4343;

        Line resultSegment = new Line(
            new Vec3d(
                (float) (p1.getX() + mua * p21.getX()),
                (float) (p1.getY() + mua * p21.getY()),
                (float) (p1.getZ() + mua * p21.getZ())
            ),
            new Vec3d(
                (float) (p3.getX() + mub * p43.getX()),
                (float) (p3.getY() + mub * p43.getY()),
                (float) (p3.getZ() + mub * p43.getZ())
            )
        );

        return resultSegment;
    }

    public Line getImmutable() {
        return new Line(point1, point2);
    }

    private static double lengthSquared(Vec3d vec) {
        return vec.dotProduct(vec);
    }

    public String toString() {
        return String.format(
            "point1 = %s, point2 = %s, midpoint = %s",
            point1 == null ? "NULL" : point1.toString(),
            point2 == null ? "NULL" : point2.toString(),
            (point1 == null || point2 == null) ? "NULL" : getMidpoint()
        );
    }
}
