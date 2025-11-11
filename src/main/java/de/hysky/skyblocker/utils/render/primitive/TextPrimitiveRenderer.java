package de.hysky.skyblocker.utils.render.primitive;

import de.hysky.skyblocker.compatibility.CaxtonCompatibility;
import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.state.TextRenderState;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.font.TextRenderer;

public final class TextPrimitiveRenderer implements PrimitiveRenderer<TextRenderState> {
	protected static final TextPrimitiveRenderer INSTANCE = new TextPrimitiveRenderer();
	private static final RenderPipeline SEE_THROUGH = CaxtonCompatibility.getSeeThroughTextPipeline().orElse(RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH);
	private static final RenderPipeline NORMAL = CaxtonCompatibility.getTextPipeline().orElse(RenderPipelines.RENDERTYPE_TEXT);

	private TextPrimitiveRenderer() {}

	@Override
	public void submitPrimitives(TextRenderState state, CameraRenderState cameraState) {
		RenderPipeline pipeline = state.throughWalls ? SEE_THROUGH : NORMAL;
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.getX()), (float) (state.pos.getY() - cameraState.pos.getY()), (float) (state.pos.getZ() - cameraState.pos.getZ()))
				.rotate(cameraState.orientation)
				.scale(state.scale, -state.scale, state.scale);

		state.glyphs.draw(new TextRenderer.GlyphDrawer() {
			@Override
			public void drawGlyph(TextDrawable glyph) {
				this.draw(glyph);
			}

			@Override
			public void drawRectangle(TextDrawable bakedGlyph) {
				this.draw(bakedGlyph);
			}

			private void draw(TextDrawable glyph) {
				TextureSetup textureSetup = RenderHelper.textureWithLightmap(glyph.textureView());
				BufferBuilder buffer = Renderer.getBuffer(pipeline, textureSetup);

				glyph.render(positionMatrix, buffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
			}
		});
	}
}
