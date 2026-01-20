package de.hysky.skyblocker.utils.render.primitive;

import de.hysky.skyblocker.compatibility.CaxtonCompatibility;
import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.state.TextRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.CameraRenderState;

public final class TextPrimitiveRenderer implements PrimitiveRenderer<TextRenderState> {
	protected static final TextPrimitiveRenderer INSTANCE = new TextPrimitiveRenderer();
	private static final RenderPipeline SEE_THROUGH = CaxtonCompatibility.getSeeThroughTextPipeline().orElse(RenderPipelines.TEXT_SEE_THROUGH);
	private static final RenderPipeline NORMAL = CaxtonCompatibility.getTextPipeline().orElse(RenderPipelines.TEXT);

	private TextPrimitiveRenderer() {}

	@Override
	public void submitPrimitives(TextRenderState state, CameraRenderState cameraState) {
		RenderPipeline pipeline = state.throughWalls ? SEE_THROUGH : NORMAL;
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.x() - cameraState.pos.x()), (float) (state.pos.y() - cameraState.pos.y()), (float) (state.pos.z() - cameraState.pos.z()))
				.rotate(cameraState.orientation)
				.scale(state.scale, -state.scale, state.scale);

		state.glyphs.visit(new Font.GlyphVisitor() {
			@Override
			public void acceptGlyph(TextRenderable.Styled glyph) {
				this.draw(glyph);
			}

			@Override
			public void acceptEffect(TextRenderable bakedGlyph) {
				this.draw(bakedGlyph);
			}

			private void draw(TextRenderable glyph) {
				TextureSetup textureSetup = TextureSetup.singleTextureWithLightmap(glyph.textureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
				BufferBuilder buffer = Renderer.getBuffer(pipeline, textureSetup);

				glyph.render(positionMatrix, buffer, LightTexture.FULL_BRIGHT, false);
			}
		});
	}
}
