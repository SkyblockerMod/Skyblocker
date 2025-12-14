package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.BufferBuilder;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CursorLineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public final class CursorLineRenderer implements PrimitiveRenderer<CursorLineRenderState> {
	protected static final CursorLineRenderer INSTANCE = new CursorLineRenderer();

	private CursorLineRenderer() {}

	@Override
	public void submitPrimitives(CursorLineRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.LINES_THROUGH_WALLS);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);

		// Start drawing the line from a point slightly in front of the camera
		Vec3 point = state.point;
		Vec3 cameraPoint = cameraState.pos.add(new Vec3(cameraState.orientation.transform(new Vector3f(0, 0, -1))));
		Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z).normalize();

		buffer.addVertex(positionMatrix, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
		.setColor(state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha)
		.setNormal(normal.x(), normal.y(), normal.z())
		.setLineWidth(state.lineWidth);

		buffer.addVertex(positionMatrix, (float) point.x(), (float) point.y(), (float) point.z())
		.setColor(state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha)
		.setNormal(normal.x(), normal.y(), normal.z())
		.setLineWidth(state.lineWidth);
	}
}
