package de.hysky.skyblocker.utils.render.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;


/**
 * Similar to {@link net.minecraft.client.gui.render.state.ColoredRectangleRenderState}
 */
public record CustomShapeGuiElementRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f matrix,
		List<Vector2f> inputVertices,
		int color,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds) implements GuiElementRenderState {
	public CustomShapeGuiElementRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, List<Vector2f> inputVertices, int color, @Nullable ScreenRectangle scissorArea
	) {
		this(pipeline, textureSetup, pose, inputVertices, color, scissorArea, createBounds(inputVertices, pose, scissorArea));
	}

	@Override
	public void buildVertices(VertexConsumer vertices, float depth) {
		for (Vector2f vector : inputVertices) {
			vertices.addVertexWith2DPose(this.matrix(), vector.x(), vector.y(), depth).setColor(color);
		}
	}

	@Nullable
	private static ScreenRectangle createBounds(List<Vector2f> inputVertices, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
		int x0 = Collections.max(inputVertices.stream().map(vertex -> (int) vertex.x).toList());
		int x1 = Collections.min(inputVertices.stream().map(vertex -> (int) vertex.x).toList());
		int y0 = Collections.max(inputVertices.stream().map(vertex -> (int) vertex.y).toList());
		int y1 = Collections.min(inputVertices.stream().map(vertex -> (int) vertex.y).toList());
		ScreenRectangle screenRect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
