package de.hysky.skyblocker.utils.render.state;

public record FilledBoxRenderState(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colourComponents, float alpha, boolean throughWalls) {
}
