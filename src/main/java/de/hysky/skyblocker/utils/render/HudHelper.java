package de.hysky.skyblocker.utils.render;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class HudHelper {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, int argb) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, texture, x, y, width, height, argb);
	}

	public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, Color color) {
		renderNineSliceColored(context, texture, x, y, width, height, ColorHelper.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
	}

	/**
	 * Draws a player head without blocking or a default head if profile is not available immediately.
	 * This fetches the profile so it will be available for future calls to this method.
	 */
	public static void drawPlayerHead(DrawContext context, int x, int y, int size, UUID uuid) {
		PlayerSkinDrawer.draw(context, SkullBlockEntity.fetchProfileByUuid(uuid).getNow(Optional.empty()).map(CLIENT.getSkinProvider()::getSkinTextures).orElseGet(() -> DefaultSkinHelper.getSkinTextures(uuid)), x, y, size);
	}

	public static void drawHorizontalGradient(DrawContext context, float startX, float startY, float endX, float endY, int colorStart, int colorEnd) {
		context.draw(provider -> {
			VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getGui());
			Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
			vertexConsumer.vertex(positionMatrix, startX, startY, 0).color(colorStart);
			vertexConsumer.vertex(positionMatrix, startX, endY, 0).color(colorStart);
			vertexConsumer.vertex(positionMatrix, endX, endY, 0).color(colorEnd);
			vertexConsumer.vertex(positionMatrix, endX, startY, 0).color(colorEnd);
		});
	}
}
