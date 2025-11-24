package de.hysky.skyblocker.utils.render;

import de.hysky.skyblocker.compatibility.CaxtonCompatibility;
import de.hysky.skyblocker.compatibility.ModernUICompatibility;
import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.HorizontalGradientGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.OutlinedTextGuiElementRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class HudHelper {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, int argb) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, argb);
	}

	public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, Color color) {
		renderNineSliceColored(context, texture, x, y, width, height, ColorHelper.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
	}

	public static void drawHorizontalGradient(DrawContext context, float startX, float startY, float endX, float endY, int colorStart, int colorEnd) {
		context.state.addSimpleElement(new HorizontalGradientGuiElementRenderState(RenderPipelines.GUI, TextureSetup.empty(), new Matrix3x2f(context.getMatrices()), (int) startX, (int) startY, (int) endX, (int) endY, colorStart, colorEnd, context.scissorStack.peekLast()));
	}

	// FIXME replace with stroked rectangles?
	public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y + 1, x + 1, y + height - 1, color);
		context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/**
	 * Draws a player head without blocking or a default head if profile is not available immediately.
	 * This fetches the profile so it will be available for future calls to this method.
	 */
	public static void drawPlayerHead(DrawContext context, int x, int y, int size, UUID uuid) {
		SkinTextures texture = CLIENT.getPlayerSkinCache().getFuture(ProfileComponent.ofDynamic(uuid))
				.getNow(Optional.empty())
				.map(PlayerSkinCache.Entry::getTextures)
				.orElseGet(() -> DefaultSkinHelper.getSkinTextures(uuid));
		PlayerSkinDrawer.draw(context, texture, x, y, size);
	}

	public static <S> void drawEquipment(DrawContext context, EquipmentRenderer equipmentRenderer, EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model<S> model, S state, ItemStack stack, int x1, int y1, int x2, int y2, float rotation, float scale, float offset) {
		EquipmentGuiElementRenderState<S> renderState = new EquipmentGuiElementRenderState<>(equipmentRenderer, layerType, assetKey, model, state, stack, x1, y1, x2, y2, rotation, scale, offset, context.scissorStack.peekLast());

		context.state.addSpecialElement(renderState);
	}

	public static void drawOutlinedText(DrawContext context, Text text, int x, int y, int color, int outlineColor) {
		OrderedText orderedText = text.asOrderedText();
		drawOutlinedText(context, orderedText, ModernUICompatibility.MODERNUI_ENABLED ? Text.literal(text.getString()).asOrderedText() : orderedText, x, y, color, outlineColor);
	}

	public static void drawOutlinedText(DrawContext context, OrderedText text, int x, int y, int color, int outlineColor) {
		drawOutlinedText(context, text, text, x, y, color, outlineColor);
	}

	private static void drawOutlinedText(DrawContext context, OrderedText text, OrderedText outlineText, int x, int y, int color, int outlineColor) {
		if (CaxtonCompatibility.drawOutlinedText(context, text, x, y, color, outlineColor)) return;
		if (ModernUICompatibility.drawOutlinedText(context, text, outlineText, x, y, color, outlineColor)) return;

		OutlinedTextGuiElementRenderState renderState = new OutlinedTextGuiElementRenderState(CLIENT.textRenderer, text, new Matrix3x2f(context.getMatrices()), x, y, color, outlineColor, false, context.scissorStack.peekLast());
		context.state.addText(renderState);
	}

	public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	// Temp fix for this?
	public static boolean hasShiftDown() {
		return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
}
