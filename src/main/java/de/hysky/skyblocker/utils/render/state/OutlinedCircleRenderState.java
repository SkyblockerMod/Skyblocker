package de.hysky.skyblocker.utils.render.state;

import net.minecraft.world.phys.Vec3;

public record OutlinedCircleRenderState(Vec3 centre, float radius, float thickness, int segments, int colour) {
}
