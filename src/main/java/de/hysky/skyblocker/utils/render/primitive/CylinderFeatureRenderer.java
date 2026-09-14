package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CylinderRenderState;

public class CylinderFeatureRenderer extends PrimitiveFeatureRenderer<CylinderFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Cylinder");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (CylinderRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(SkyblockerRenderPipelines.CYLINDER);

				float halfHeight = state.height() / 2.0f;

				for (int i = 0; i <= state.segments(); i++) {
					double angle = Math.TAU * i / state.segments();
					float dx = (float) Math.cos(angle) * state.radius();
					float dz = (float) Math.sin(angle) * state.radius();

					builder.addVertex(positionMatrix, (float) state.centre().x() + dx, (float) state.centre().y() + halfHeight, (float) state.centre().z() + dz).setColor(state.colour());
					builder.addVertex(positionMatrix, (float) state.centre().x() + dx, (float) state.centre().y() - halfHeight, (float) state.centre().z() + dz).setColor(state.colour());
				}
			}
		}
	}

	public record Submit(List<CylinderRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
