package de.hysky.skyblocker.utils.render.state;

import net.minecraft.world.phys.Vec3;

public record SphereRenderState(Vec3 centre, float radius, int segments, int rings, int colour) {
}
