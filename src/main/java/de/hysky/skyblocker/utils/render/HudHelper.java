package de.hysky.skyblocker.utils.render;

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;

import org.joml.Matrix3x2f;

import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.HorizontalGradientGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.OutlinedTextGuiElementRenderState;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

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

	/**
	 * Draws a player head without blocking or a default head if profile is not available immediately.
	 * This fetches the profile so it will be available for future calls to this method.
	 */
	public static void drawPlayerHead(DrawContext context, int x, int y, int size, UUID uuid) {
		PlayerSkinDrawer.draw(context, SkullBlockEntity.fetchProfileByUuid(uuid).getNow(Optional.empty()).map(CLIENT.getSkinProvider()::getSkinTextures).orElseGet(() -> DefaultSkinHelper.getSkinTextures(uuid)), x, y, size);
	}

	public static void drawEquipment(DrawContext context, EquipmentRenderer equipmentRenderer, EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model model, ItemStack stack, int x1, int y1, int x2, int y2, float rotation, float scale, float offset) {
		EquipmentGuiElementRenderState renderState = new EquipmentGuiElementRenderState(equipmentRenderer, layerType, assetKey, model, stack, x1, y1, x2, y2, rotation, scale, offset, context.scissorStack.peekLast());

		context.state.addSpecialElement(renderState);
	}

	public static void drawOutlinedText(DrawContext context, OrderedText text, int x, int y, int color, int outlineColor) {
		OutlinedTextGuiElementRenderState renderState = new OutlinedTextGuiElementRenderState(CLIENT.textRenderer, text, new Matrix3x2f(context.getMatrices()), x, y, color, outlineColor, false, context.scissorStack.peekLast());

		context.state.addText(renderState);
	}

    public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
}
