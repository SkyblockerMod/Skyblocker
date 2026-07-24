package de.hysky.skyblocker.utils.render.state;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record TexturedQuadRenderState(Vec3 pos, float width, float height, float textureWidth, float textureHeight, Vec3 renderOffset, Identifier texture, float[] shaderColour, float alpha, boolean throughWalls) {
}
