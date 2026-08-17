package de.hysky.skyblocker.utils.render.state;

import net.minecraft.world.phys.Vec3;

public record QuadRenderState(Vec3[] points, float[] colourComponents, float alpha, boolean throughWalls) {
}
