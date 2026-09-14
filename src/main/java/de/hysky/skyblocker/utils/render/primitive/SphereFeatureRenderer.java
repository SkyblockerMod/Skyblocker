package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.SphereRenderState;

public class SphereFeatureRenderer extends PrimitiveFeatureRenderer<SphereFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Sphere");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (SphereRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(SkyblockerRenderPipelines.CYLINDER);

				for (int lat = 0; lat < state.rings(); lat++) {
					double lat0 = Math.PI * (double) lat / state.rings();
					double lat1 = Math.PI * (double) (lat + 1) / state.rings();

					float y0 = (float) Math.cos(lat0) * state.radius();
					float y1 = (float) Math.cos(lat1) * state.radius();

					float r0 = (float) Math.sin(lat0) * state.radius();
					float r1 = (float) Math.sin(lat1) * state.radius();

					for (int lon = 0; lon <= state.segments(); lon++) {
						double angle = Math.TAU * (double) lon / state.segments();
						float x0 = (float) Math.cos(angle);
						float z0 = (float) Math.sin(angle);

						// First Triangle
						builder.addVertex(positionMatrix,
								Math.fma(x0, r0, (float) state.centre().x()),
								(float) state.centre().y() + y0,
								Math.fma(z0, r0, (float) state.centre().z()))
						.setColor(state.colour());

						builder.addVertex(positionMatrix,
								Math.fma(x0, r1, (float) state.centre().x()),
								(float) state.centre().y() + y1,
								Math.fma(z0, r1, (float) state.centre().z()))
						.setColor(state.colour());
					}
				}
			}
		}
	}

	public record Submit(List<SphereRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
