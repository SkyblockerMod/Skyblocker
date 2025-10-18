package de.hysky.skyblocker.utils.render.state;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.Vec3d;

public class TextRenderState {
	public TextRenderer.GlyphDrawable glyphs;
	public Vec3d pos;
	public float scale;
	public float yOffset;
	public boolean throughWalls;
}
