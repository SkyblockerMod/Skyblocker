package de.hysky.skyblocker.utils.render.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class TexturedQuadRenderState {
	public Vec3 pos;
	public float width;
	public float height;
	public float textureWidth;
	public float textureHeight;
	public Vec3 renderOffset;
	public ResourceLocation texture;
	public float[] shaderColour;
	public float alpha;
	public boolean throughWalls;
}
