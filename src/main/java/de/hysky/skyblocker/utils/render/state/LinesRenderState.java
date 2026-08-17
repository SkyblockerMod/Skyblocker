package de.hysky.skyblocker.utils.render.state;

import net.minecraft.world.phys.Vec3;

public record LinesRenderState(Vec3[] points, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
}
