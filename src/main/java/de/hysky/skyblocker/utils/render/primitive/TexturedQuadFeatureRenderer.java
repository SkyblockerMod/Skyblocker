package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.TexturedQuadRenderState;

public class TexturedQuadFeatureRenderer extends PrimitiveFeatureRenderer<TexturedQuadFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Textured Quad");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			for (TexturedQuadRenderState state : submit.states()) {
				TextureSetup textureSetup = TextureSetup.singleTexture(context.textureManager().getTexture(state.texture()).getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
				VertexConsumer builder = this.getVertexBuilder(submit.throughWalls() ? SkyblockerRenderPipelines.TEXTURE_THROUGH_WALLS : SkyblockerRenderPipelines.TEXTURE, textureSetup);
				Matrix4f positionMatrix = new Matrix4f()
						.translate((float) (state.pos().x() - submit.camera().pos.x()), (float) (state.pos().y() - submit.camera().pos.y()), (float) (state.pos().z() - submit.camera().pos.z()))
						.rotate(submit.camera().orientation);

				builder.addVertex(positionMatrix, (float) state.renderOffset().x(), (float) state.renderOffset().y(), (float) state.renderOffset().z()).setUv(1, 1 - state.textureHeight()).setColor(state.shaderColour()[0], state.shaderColour()[1], state.shaderColour()[2], state.alpha());
				builder.addVertex(positionMatrix, (float) state.renderOffset().x(), (float) state.renderOffset().y() + state.height(), (float) state.renderOffset().z()).setUv(1, 1).setColor(state.shaderColour()[0], state.shaderColour()[1], state.shaderColour()[2], state.alpha());
				builder.addVertex(positionMatrix, (float) state.renderOffset().x() + state.width(), (float) state.renderOffset().y() + state.height(), (float) state.renderOffset().z()).setUv(1 - state.textureWidth(), 1).setColor(state.shaderColour()[0], state.shaderColour()[1], state.shaderColour()[2], state.alpha());
				builder.addVertex(positionMatrix, (float) state.renderOffset().x() + state.width(), (float) state.renderOffset().y(), (float) state.renderOffset().z()).setUv(1 - state.textureWidth(), 1 - state.textureHeight()).setColor(state.shaderColour()[0], state.shaderColour()[1], state.shaderColour()[2], state.alpha());
			}
		}
	}

	public record Submit(List<TexturedQuadRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
