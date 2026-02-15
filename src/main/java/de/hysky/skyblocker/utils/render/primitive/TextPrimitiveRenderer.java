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
import org.jspecify.annotations.Nullable;

public final class TextPrimitiveRenderer implements PrimitiveRenderer<TextRenderState> {
	protected static final TextPrimitiveRenderer INSTANCE = new TextPrimitiveRenderer();
	private static final @Nullable RenderPipeline CAXTON_SEE_THROUGH = CaxtonCompatibility.getSeeThroughTextPipeline().orElse(null);
	private static final @Nullable RenderPipeline CAXTON_NORMAL = CaxtonCompatibility.getTextPipeline().orElse(null);

	private static RenderPipeline getPipeline(boolean seeThrough, boolean intensity) {
		if (seeThrough) {
			return CAXTON_SEE_THROUGH != null ? CAXTON_SEE_THROUGH : (intensity ? RenderPipelines.TEXT_INTENSITY_SEE_THROUGH : RenderPipelines.TEXT_SEE_THROUGH);
		} else {
			return CAXTON_NORMAL != null ? CAXTON_NORMAL : (intensity ? RenderPipelines.TEXT_INTENSITY : RenderPipelines.TEXT);
		}
	}

	private TextPrimitiveRenderer() {}

	@Override
	public void submitPrimitives(TextRenderState state, CameraRenderState cameraState) {
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
				// This is a bit of a weird workaround to know if the intensity pipelines should be used instead of the normal ones.
				// Normally GlyphBitmap#isColored should be used to figure that out, but we don't have access to it here
				BufferBuilder buffer = Renderer.getBuffer(getPipeline(state.throughWalls, glyph.guiPipeline() == RenderPipelines.GUI_TEXT_INTENSITY), textureSetup);

				glyph.render(positionMatrix, buffer, LightTexture.FULL_BRIGHT, false);
			}
		});
	}
}
