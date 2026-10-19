package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CursorLineRenderState;

public class CursorLineFeatureRenderer extends PrimitiveFeatureRenderer<CursorLineFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Cursor Line");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (CursorLineRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(SkyblockerRenderPipelines.LINES_THROUGH_WALLS);

				// Start drawing the line from a point slightly in front of the camera
				Vec3 point = state.point();
				Vec3 cameraPoint = submit.camera().pos.add(new Vec3(submit.camera().orientation.transform(new Vector3f(0, 0, -1))));
				Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z).normalize();

				builder.addVertex(positionMatrix, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
				.setColor(state.colourComponents()[0], state.colourComponents()[1], state.colourComponents()[2], state.alpha())
				.setNormal(normal.x(), normal.y(), normal.z())
				.setLineWidth(state.lineWidth());

				builder.addVertex(positionMatrix, (float) point.x(), (float) point.y(), (float) point.z())
				.setColor(state.colourComponents()[0], state.colourComponents()[1], state.colourComponents()[2], state.alpha())
				.setNormal(normal.x(), normal.y(), normal.z())
				.setLineWidth(state.lineWidth());
			}
		}
	}

	public record Submit(List<CursorLineRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
