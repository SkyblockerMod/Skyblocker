package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.QuadRenderState;

public class QuadFeatureRenderer extends PrimitiveFeatureRenderer<QuadFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Quad");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (QuadRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughWalls() ? SkyblockerRenderPipelines.QUADS_THROUGH_WALLS : RenderPipelines.DEBUG_QUADS);

				for (int i = 0; i < 4; i++) {
					builder.addVertex(positionMatrix, (float) state.points()[i].x(), (float) state.points()[i].y(), (float) state.points()[i].z())
					.setColor(state.colourComponents()[0], state.colourComponents()[1], state.colourComponents()[2], state.alpha());
				}
			}
		}
	}

	public record Submit(List<QuadRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
