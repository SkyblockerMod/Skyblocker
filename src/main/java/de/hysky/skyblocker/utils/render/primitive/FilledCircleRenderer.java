package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import de.hysky.skyblocker.utils.render.state.FilledCircleRenderState;
import net.minecraft.client.render.BufferBuilder;

public final class FilledCircleRenderer implements PrimitiveRenderer<FilledCircleRenderState> {
	protected static final FilledCircleRenderer INSTANCE = new FilledCircleRenderer();

	private FilledCircleRenderer() {}

	@Override
	public void submitPrimitives(FilledCircleRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.CIRCLE);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);

		for (int i = 0; i <= state.segments; i++) {
			double angle = Math.TAU * i / state.segments;
			float dx = (float) Math.cos(angle) * state.radius;
			float dz = (float) Math.sin(angle) * state.radius;

			buffer.vertex(positionMatrix, (float) state.centre.getX() + dx, (float) state.centre.getY(), (float) state.centre.getZ() + dz).color(state.colour);
		}
	}
}
