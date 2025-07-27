package de.hysky.skyblocker.utils.render.gui.state;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;

/**
 * Similar to {@link net.minecraft.client.gui.render.state.ColoredQuadGuiElementRenderState}
 */
public record HorizontalGradientGuiElementRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f matrix,
		int startX,
		int startY,
		int endX,
		int endY,
		int colorStart,
		int colorEnd,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds) implements SimpleGuiElementRenderState {
	public HorizontalGradientGuiElementRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, @Nullable ScreenRect scissorArea
		) {
			this(pipeline, textureSetup, pose, startX, startY, endX, endY, colorStart, colorEnd, scissorArea, createBounds(startX, startY, endX, endY, pose, scissorArea));
		}

	@Override
	public void setupVertices(VertexConsumer vertices, float depth) {
		vertices.vertex(this.matrix(), this.startX(), this.startY(), depth).color(this.colorStart());
		vertices.vertex(this.matrix(), this.startX(), this.endY(), depth).color(this.colorStart());
		vertices.vertex(this.matrix(), this.endX(), this.endY(), depth).color(this.colorEnd());
		vertices.vertex(this.matrix(), this.endX(), this.startY(), depth).color(this.colorEnd());
	}

	@Nullable
	private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
		ScreenRect screenRect = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
