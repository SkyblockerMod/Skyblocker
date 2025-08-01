package de.hysky.skyblocker.utils.render.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.Collections;
import java.util.List;


/**
 * Similar to {@link net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState}
 */
public record CustomShapeGuiElementRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f matrix,
		List<Vector2f> inputVertices,
		int color,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds) implements SimpleGuiElementRenderState {
	public CustomShapeGuiElementRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, List<Vector2f> inputVertices, int color, @Nullable ScreenRect scissorArea
	) {
		this(pipeline, textureSetup, pose, inputVertices, color, scissorArea, createBounds(inputVertices, pose, scissorArea));
	}

	@Override
	public void setupVertices(VertexConsumer vertices, float depth) {
		for (Vector2f vector : inputVertices) {
			vertices.vertex(this.matrix(), vector.x(), vector.y(), depth).color(color);
		}
	}

	@Nullable
	private static ScreenRect createBounds(List<Vector2f> inputVertices, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
		int x0 = Collections.max(inputVertices.stream().map(vertex -> (int) vertex.x).toList());
		int x1 = Collections.min(inputVertices.stream().map(vertex -> (int) vertex.x).toList());
		int y0 = Collections.max(inputVertices.stream().map(vertex -> (int) vertex.y).toList());
		int y1 = Collections.min(inputVertices.stream().map(vertex -> (int) vertex.y).toList());
		ScreenRect screenRect = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
