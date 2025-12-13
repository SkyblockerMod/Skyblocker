package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;

public final class OutlinedBoxRenderer implements PrimitiveRenderer<OutlinedBoxRenderState> {
	protected static final OutlinedBoxRenderer INSTANCE = new OutlinedBoxRenderer();

	private OutlinedBoxRenderer() {}

	@Override
	public void submitPrimitives(OutlinedBoxRenderState state, CameraRenderState cameraState) {
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);
		float minX = (float) state.minX;
		float minY = (float) state.minY;
		float minZ = (float) state.minZ;
		float maxX = (float) state.maxX;
		float maxY = (float) state.maxY;
		float maxZ = (float) state.maxZ;
		float red = state.colourComponents[0];
		float green = state.colourComponents[1];
		float blue = state.colourComponents[2];
		float alpha = state.alpha;

		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, minY, minZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, minY, maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, minY, minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, maxY, minZ).color(red, green, blue, alpha).normal(-1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha).normal(-1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0f, -1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, minY, maxZ).color(red, green, blue, alpha).normal(0.0f, -1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, minY, maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, -1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, minY, minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, -1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, minX, maxY, maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f).lineWidth(state.lineWidth);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f).lineWidth(state.lineWidth);
	}
}
