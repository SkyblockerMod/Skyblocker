package de.hysky.skyblocker.utils.render.state;

import net.minecraft.client.gui.Font;
import net.minecraft.world.phys.Vec3;

public record TextRenderState(Font.PreparedText glyphs, Vec3 pos, float scale, float yOffset, boolean throughWalls) {
}
