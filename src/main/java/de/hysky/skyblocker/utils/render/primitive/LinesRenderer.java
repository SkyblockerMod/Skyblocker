package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import de.hysky.skyblocker.utils.render.state.LinesRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Vec3d;

public final class LinesRenderer implements PrimitiveRenderer<LinesRenderState> {
	protected static final LinesRenderer INSTANCE = new LinesRenderer();

	private LinesRenderer() {}

	@Override
	public void submitPrimitives(LinesRenderState state, CameraRenderState cameraState) {
		Vec3d[] points = state.points;
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES, state.lineWidth);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);

		for (int i = 0; i < points.length; i++) {
			Vec3d nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
			Vector3f normalVec = nextPoint.toVector3f().sub((float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).normalize();

			// If the last point, the normal is the previous point minus the current point.
			// Negate the normal to make it point forward, away from the previous point.
			if (i + 1 == points.length) {
				normalVec.negate();
			}

			buffer.vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
			.color(state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha)
			.normal(normalVec.x(), normalVec.y(), normalVec.z());
		}
	}
}
