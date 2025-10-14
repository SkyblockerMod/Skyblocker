package de.hysky.skyblocker.utils.render.state;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class TexturedQuadRenderState {
	public Vec3d pos;
	public float width;
	public float height;
	public float textureWidth;
	public float textureHeight;
	public Vec3d renderOffset;
	public Identifier texture;
	public float[] shaderColour;
	public float alpha;
	public boolean throughWalls;
}
