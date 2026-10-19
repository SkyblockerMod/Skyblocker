package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.OutlinedCircleRenderState;

public class OutlinedCircleFeatureRenderer extends PrimitiveFeatureRenderer<OutlinedCircleFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Outlined Circle");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (OutlinedCircleRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(SkyblockerRenderPipelines.CIRCLE_LINES);

				float innerRadius = state.radius() - state.thickness() / 2f;
				float outerRadius = state.radius() + state.thickness() / 2f;

				for (int i = 0; i < state.segments(); i++) {
					double angle1 = Math.TAU * i / state.segments();
					double angle2 = Math.TAU * (i + 1) / state.segments();

					float x1Inner = (float) Math.cos(angle1) * innerRadius;
					float z1Inner = (float) Math.sin(angle1) * innerRadius;

					float x1Outer = (float) Math.cos(angle1) * outerRadius;
					float z1Outer = (float) Math.sin(angle1) * outerRadius;

					float x2Inner = (float) Math.cos(angle2) * innerRadius;
					float z2Inner = (float) Math.sin(angle2) * innerRadius;

					float x2Outer = (float) Math.cos(angle2) * outerRadius;
					float z2Outer = (float) Math.sin(angle2) * outerRadius;

					float cx = (float) state.centre().x();
					float cy = (float) state.centre().y();
					float cz = (float) state.centre().z();

					// Each quad is formed from two triangles
					builder.addVertex(positionMatrix, cx + x1Inner, cy, cz + z1Inner).setColor(state.colour());
					builder.addVertex(positionMatrix, cx + x1Outer, cy, cz + z1Outer).setColor(state.colour());
					builder.addVertex(positionMatrix, cx + x2Outer, cy, cz + z2Outer).setColor(state.colour());
					builder.addVertex(positionMatrix, cx + x2Inner, cy, cz + z2Inner).setColor(state.colour());
				}
			}
		}
	}

	public record Submit(List<OutlinedCircleRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
