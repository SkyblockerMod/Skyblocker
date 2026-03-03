package de.hysky.skyblocker.utils.render.gui.state;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;

/**
 * Similar to {@link net.minecraft.client.gui.render.state.ColoredRectangleRenderState}
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
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds) implements GuiElementRenderState {
	public HorizontalGradientGuiElementRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, @Nullable ScreenRectangle scissorArea
		) {
			this(pipeline, textureSetup, pose, startX, startY, endX, endY, colorStart, colorEnd, scissorArea, createBounds(startX, startY, endX, endY, pose, scissorArea));
		}

	@Override
	public void buildVertices(VertexConsumer vertices) {
		vertices.addVertexWith2DPose(this.matrix(), this.startX(), this.startY()).setColor(this.colorStart());
		vertices.addVertexWith2DPose(this.matrix(), this.startX(), this.endY()).setColor(this.colorStart());
		vertices.addVertexWith2DPose(this.matrix(), this.endX(), this.endY()).setColor(this.colorEnd());
		vertices.addVertexWith2DPose(this.matrix(), this.endX(), this.startY()).setColor(this.colorEnd());
	}

	@Nullable
	private static ScreenRectangle createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
		ScreenRectangle screenRect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
