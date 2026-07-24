package de.hysky.skyblocker.utils.render.state;

import net.minecraft.world.phys.Vec3;

public record CylinderRenderState(Vec3 centre, float radius, float height, int segments, int colour) {
}
