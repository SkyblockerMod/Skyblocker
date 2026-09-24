package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.LinesRenderState;

public class LinesFeatureRenderer extends PrimitiveFeatureRenderer<LinesFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Lines");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (LinesRenderState state : submit.states()) {
				Vec3[] points = state.points();
				VertexConsumer builder = this.getVertexBuilder(submit.throughWalls() ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES);

				for (int i = 0; i < points.length; i++) {
					Vec3 nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
					Vector3f normalVec = nextPoint.toVector3f().sub((float) points[i].x(), (float) points[i].y(), (float) points[i].z()).normalize();

					// If the last point, the normal is the previous point minus the current point.
					// Negate the normal to make it point forward, away from the previous point.
					if (i + 1 == points.length) {
						normalVec.negate();
					}

					builder.addVertex(positionMatrix, (float) points[i].x(), (float) points[i].y(), (float) points[i].z())
					.setColor(state.colourComponents()[0], state.colourComponents()[1], state.colourComponents()[2], state.alpha())
					.setNormal(normalVec.x(), normalVec.y(), normalVec.z())
					.setLineWidth(state.lineWidth());
				}
			}
		}
	}

	public record Submit(List<LinesRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
