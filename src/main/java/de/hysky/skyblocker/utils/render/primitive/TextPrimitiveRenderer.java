package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import de.hysky.skyblocker.utils.render.state.TextRenderState;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.BakedGlyph.DrawnGlyph;
import net.minecraft.client.font.BakedGlyph.Rectangle;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.font.TextRenderer;

public final class TextPrimitiveRenderer implements PrimitiveRenderer<TextRenderState> {
	protected static final TextPrimitiveRenderer INSTANCE = new TextPrimitiveRenderer();

	private TextPrimitiveRenderer() {}

	@Override
	public void submitPrimitives(TextRenderState state, CameraRenderState cameraState) {
		RenderPipeline pipeline = state.throughWalls ? RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH : RenderPipelines.RENDERTYPE_TEXT;
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.getX()), (float) (state.pos.getY() - cameraState.pos.getY()), (float) (state.pos.getZ() - cameraState.pos.getZ()))
				.rotate(cameraState.rotation)
				.scale(state.scale, -state.scale, state.scale);

		state.glyphs.draw(new TextRenderer.GlyphDrawer() {
			@Override
			public void drawGlyph(DrawnGlyph glyph) {
				BakedGlyph bakedGlyph = glyph.glyph();
				TextureSetup textureSetup = RenderHelper.textureWithLightmap(bakedGlyph.getTexture());
				BufferBuilder buffer = Renderer.getBuffer(pipeline, textureSetup);

				bakedGlyph.draw(glyph, positionMatrix, buffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
			}

			@Override
			public void drawRectangle(BakedGlyph bakedGlyph, Rectangle rect) {
				TextureSetup textureSetup = RenderHelper.textureWithLightmap(bakedGlyph.getTexture());
				BufferBuilder buffer = Renderer.getBuffer(pipeline, textureSetup);

				bakedGlyph.drawRectangle(rect, positionMatrix, buffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
			}
		});
	}
}
