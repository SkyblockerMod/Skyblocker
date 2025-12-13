package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CursorLineRenderState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.Vec3d;

public final class CursorLineRenderer implements PrimitiveRenderer<CursorLineRenderState> {
	protected static final CursorLineRenderer INSTANCE = new CursorLineRenderer();

	private CursorLineRenderer() {}

	@Override
	public void submitPrimitives(CursorLineRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.LINES_THROUGH_WALLS);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);

		// Start drawing the line from a point slightly in front of the camera
		Vec3d point = state.point;
		Vec3d cameraPoint = cameraState.pos.add(new Vec3d(cameraState.orientation.transform(new Vector3f(0, 0, -1))));
		Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z).normalize();

		buffer.vertex(positionMatrix, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
		.color(state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha)
		.normal(normal.x(), normal.y(), normal.z())
		.lineWidth(state.lineWidth);

		buffer.vertex(positionMatrix, (float) point.getX(), (float) point.getY(), (float) point.getZ())
		.color(state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha)
		.normal(normal.x(), normal.y(), normal.z())
		.lineWidth(state.lineWidth);
	}
}
