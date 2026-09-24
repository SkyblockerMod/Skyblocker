package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.FilledCircleRenderState;

public class FilledCircleFeatureRenderer extends PrimitiveFeatureRenderer<FilledCircleFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Filled Circle");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (FilledCircleRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(SkyblockerRenderPipelines.CIRCLE);

				for (int i = 0; i <= state.segments(); i++) {
					double angle = Math.TAU * i / state.segments();
					float dx = (float) Math.cos(angle) * state.radius();
					float dz = (float) Math.sin(angle) * state.radius();

					builder.addVertex(positionMatrix, (float) state.centre().x() + dx, (float) state.centre().y(), (float) state.centre().z() + dz).setColor(state.colour());
				}
			}
		}
	}

	public record Submit(List<FilledCircleRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
