package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;

public final class FilledBoxRenderer implements PrimitiveRenderer<FilledBoxRenderState> {
	protected static final FilledBoxRenderer INSTANCE = new FilledBoxRenderer();

	private FilledBoxRenderer() {}

	@Override
	public void submitPrimitives(FilledBoxRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.FILLED_THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);
		MatrixStack matrices = MatrixHelper.toStack(positionMatrix);

		VertexRendering.drawFilledBox(matrices, buffer, state.minX, state.minY, state.minZ, state.maxX, state.maxY, state.maxZ, state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha);
	}
}
