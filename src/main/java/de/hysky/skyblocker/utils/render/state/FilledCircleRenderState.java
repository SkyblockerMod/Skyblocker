package de.hysky.skyblocker.utils.render.state;

import net.minecraft.world.phys.Vec3;

public record FilledCircleRenderState(Vec3 centre, float radius, int segments, int colour) {
}
