package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.BufferBuilder;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.QuadRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.CameraRenderState;

public final class QuadRenderer implements PrimitiveRenderer<QuadRenderState> {
	protected static final QuadRenderer INSTANCE = new QuadRenderer();

	private QuadRenderer() {}

	@Override
	public void submitPrimitives(QuadRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.QUADS_THROUGH_WALLS : RenderPipelines.DEBUG_QUADS);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);

		for (int i = 0; i < 4; i++) {
			buffer.addVertex(positionMatrix, (float) state.points[i].x(), (float) state.points[i].y(), (float) state.points[i].z())
			.setColor(state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha);
		}
	}
}
