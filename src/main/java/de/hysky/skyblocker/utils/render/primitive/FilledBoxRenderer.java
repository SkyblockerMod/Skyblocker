package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;

public final class FilledBoxRenderer implements PrimitiveRenderer<FilledBoxRenderState> {
	protected static final FilledBoxRenderer INSTANCE = new FilledBoxRenderer();

	private FilledBoxRenderer() {}

	@Override
	public void submitPrimitives(FilledBoxRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.FILLED_THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);
		PoseStack matrices = MatrixHelper.toStack(positionMatrix);

		ShapeRenderer.addChainedFilledBoxVertices(matrices, buffer, state.minX, state.minY, state.minZ, state.maxX, state.maxY, state.maxZ, state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha);
	}
}
