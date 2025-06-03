package de.hysky.skyblocker.utils.render;

import java.awt.Color;

import org.joml.Matrix3x2f;

import de.hysky.skyblocker.mixins.accessors.DrawContextAccessor;
import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.HorizontalGradientGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.OutlinedTextGuiElementRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.texture.TextureSetup;
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
		DrawContextAccessor accessor = (DrawContextAccessor) context;

		accessor.getState().addSimpleElement(new HorizontalGradientGuiElementRenderState(RenderPipelines.GUI, TextureSetup.empty(), new Matrix3x2f(context.getMatrices()), (int) startX, (int) startY, (int) endX, (int) endY, colorStart, colorEnd, accessor.getScissorStack().peekLast()));
	}

	public static void drawEquipment(DrawContext context, EquipmentRenderer equipmentRenderer, EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model model, ItemStack stack, int x1, int y1, int x2, int y2, float rotation, float scale, float offset) {
		DrawContextAccessor accessor = (DrawContextAccessor) context;
		EquipmentGuiElementRenderState renderState = new EquipmentGuiElementRenderState(equipmentRenderer, layerType, assetKey, model, stack, x1, y1, x2, y2, rotation, scale, offset, accessor.getScissorStack().peekLast());

		accessor.getState().addSpecialElement(renderState);
	}

	public static void drawOutlinedText(DrawContext context, OrderedText text, int x, int y, int color, int outlineColor) {
		DrawContextAccessor accessor = (DrawContextAccessor) context;
		OutlinedTextGuiElementRenderState renderState = new OutlinedTextGuiElementRenderState(CLIENT.textRenderer, text, new Matrix3x2f(context.getMatrices()), x, y, color, outlineColor, false, accessor.getScissorStack().peekLast());

		accessor.getState().addText(renderState);
	}
}
