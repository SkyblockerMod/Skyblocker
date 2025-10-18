package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedCircleRenderState;
import net.minecraft.client.render.BufferBuilder;

public final class OutlinedCircleRenderer implements PrimitiveRenderer<OutlinedCircleRenderState> {
	protected static final OutlinedCircleRenderer INSTANCE = new OutlinedCircleRenderer();

	private OutlinedCircleRenderer() {}

	@Override
	public void submitPrimitives(OutlinedCircleRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.CIRCLE_LINES);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);

		float innerRadius = state.radius - state.thickness / 2f;
		float outerRadius = state.radius + state.thickness / 2f;

		for (int i = 0; i < state.segments; i++) {
			double angle1 = Math.TAU * i / state.segments;
			double angle2 = Math.TAU * (i + 1) / state.segments;

			float x1Inner = (float) Math.cos(angle1) * innerRadius;
			float z1Inner = (float) Math.sin(angle1) * innerRadius;

			float x1Outer = (float) Math.cos(angle1) * outerRadius;
			float z1Outer = (float) Math.sin(angle1) * outerRadius;

			float x2Inner = (float) Math.cos(angle2) * innerRadius;
			float z2Inner = (float) Math.sin(angle2) * innerRadius;

			float x2Outer = (float) Math.cos(angle2) * outerRadius;
			float z2Outer = (float) Math.sin(angle2) * outerRadius;

			float cx = (float) state.centre.getX();
			float cy = (float) state.centre.getY();
			float cz = (float) state.centre.getZ();

			// Each quad is formed from two triangles
			buffer.vertex(positionMatrix, cx + x1Inner, cy, cz + z1Inner).color(state.colour);
			buffer.vertex(positionMatrix, cx + x1Outer, cy, cz + z1Outer).color(state.colour);
			buffer.vertex(positionMatrix, cx + x2Outer, cy, cz + z2Outer).color(state.colour);
			buffer.vertex(positionMatrix, cx + x2Inner, cy, cz + z2Inner).color(state.colour);
		}
	}
}
