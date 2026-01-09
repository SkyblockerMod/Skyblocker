package de.hysky.skyblocker.utils.render.state;

import net.minecraft.client.gui.Font;
import net.minecraft.world.phys.Vec3;

public class TextRenderState {
	public Font.PreparedText glyphs;
	public Vec3 pos;
	public float scale;
	public float yOffset;
	public boolean throughWalls;
}
