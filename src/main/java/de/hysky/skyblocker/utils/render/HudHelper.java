package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import de.hysky.skyblocker.compatibility.CaxtonCompatibility;
import de.hysky.skyblocker.compatibility.ModernUICompatibility;
import de.hysky.skyblocker.mixins.accessors.GuiGraphicsInvoker;
import de.hysky.skyblocker.utils.render.gui.state.CustomShapeGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.HorizontalGradientGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.OutlinedTextGuiElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public class HudHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	/**
	 * Suitable for rendering two blurred rectangles at once
	 */
	private static final TexturePool BLIT_TEXTURE_POOL = TexturePool.create("Blit Pool", 4, GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST, TextureFormat.RGBA8);

	public static void renderNineSliceColored(GuiGraphics context, ResourceLocation texture, int x, int y, int width, int height, int argb) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, argb);
	}

	public static void renderNineSliceColored(GuiGraphics context, ResourceLocation texture, int x, int y, int width, int height, Color color) {
		renderNineSliceColored(context, texture, x, y, width, height, ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
	}

	public static void drawHorizontalGradient(GuiGraphics context, float startX, float startY, float endX, float endY, int colorStart, int colorEnd) {
		context.guiRenderState.submitGuiElement(new HorizontalGradientGuiElementRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(context.pose()), (int) startX, (int) startY, (int) endX, (int) endY, colorStart, colorEnd, context.scissorStack.peek()));
	}

	// FIXME replace with stroked rectangles?
	public static void drawBorder(GuiGraphics context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y + 1, x + 1, y + height - 1, color);
		context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/**
	 * Draws shape with given vertices. Note the vertices must be in the right or for this to work properly this function does not sort them for you.
	 * @param context draw context
	 * @param vertices vertices of shape
	 * @param color color of shape
	 */
	public static void drawCustomShape(GuiGraphics context, List<Vector2f> vertices, int color) {
		context.guiRenderState.submitGuiElement(new CustomShapeGuiElementRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(context.pose()), vertices, color, context.scissorStack.peek()));
	}

	/**
	 * Draws a player head without blocking or a default head if profile is not available immediately.
	 * This fetches the profile so it will be available for future calls to this method.
	 */
	public static void drawPlayerHead(GuiGraphics context, int x, int y, int size, UUID uuid) {
		PlayerSkin texture = CLIENT.playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(uuid))
				.getNow(Optional.empty())
				.map(PlayerSkinRenderCache.RenderInfo::playerSkin)
				.orElseGet(() -> DefaultPlayerSkin.get(uuid));
		PlayerFaceRenderer.draw(context, texture, x, y, size);
	}

	public static <S> void drawEquipment(GuiGraphics context, EquipmentLayerRenderer equipmentRenderer, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> assetKey, Model<S> model, S state, ItemStack stack, int x1, int y1, int x2, int y2, float rotation, float scale, float offset) {
		EquipmentGuiElementRenderState<S> renderState = new EquipmentGuiElementRenderState<>(equipmentRenderer, layerType, assetKey, model, state, stack, x1, y1, x2, y2, rotation, scale, offset, context.scissorStack.peek());

		context.guiRenderState.submitPicturesInPictureState(renderState);
	}

	public static void drawOutlinedText(GuiGraphics context, Component text, int x, int y, int color, int outlineColor) {
		FormattedCharSequence orderedText = text.getVisualOrderText();
		drawOutlinedText(context, orderedText, ModernUICompatibility.MODERNUI_ENABLED ? Component.literal(text.getString()).getVisualOrderText() : orderedText, x, y, color, outlineColor);
	}

	public static void drawOutlinedText(GuiGraphics context, FormattedCharSequence text, int x, int y, int color, int outlineColor) {
		drawOutlinedText(context, text, text, x, y, color, outlineColor);
	}

	private static void drawOutlinedText(GuiGraphics context, FormattedCharSequence text, FormattedCharSequence outlineText, int x, int y, int color, int outlineColor) {
		if (CaxtonCompatibility.drawOutlinedText(context, text, x, y, color, outlineColor)) return;
		if (ModernUICompatibility.drawOutlinedText(context, text, outlineText, x, y, color, outlineColor)) return;

		OutlinedTextGuiElementRenderState renderState = new OutlinedTextGuiElementRenderState(CLIENT.font, text, new Matrix3x2f(context.pose()), x, y, color, outlineColor, false, context.scissorStack.peek());
		context.guiRenderState.submitText(renderState);
	}

	/**
	 * Submits a blurred rectangle to be rendered at the given position.
	 *
	 * @param radius The strength of the blur, must be positive.
	 */
	public static void submitBlurredRectangle(GuiGraphics graphics, int x0, int y0, int x1, int y1, int radius) {
		RenderTarget mainRenderTarget = CLIENT.getMainRenderTarget();
		int requiredWidth = mainRenderTarget.width;
		int requiredHeight = mainRenderTarget.height;

		int index = BLIT_TEXTURE_POOL.getNextAvailableIndex(requiredWidth, requiredHeight);
		GpuTexture blitTexture = BLIT_TEXTURE_POOL.getTexture(index);
		GpuTextureView blitTextureView = BLIT_TEXTURE_POOL.getTextureView(index);
		// The sampler needs to be linear in order for the shader sampling interpolation trick to work properly
		blitTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
		blitTexture.setTextureFilter(FilterMode.LINEAR, false);
		// Pass the radius through the vertex colour - least painful way to do this
		int vertexColour = ARGB.color(radius, 255, 255);

		// Copy the main render target colour texture to our temporary one since you cannot read from and write to the same texture in a single draw.
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(mainRenderTarget.getColorTexture(), blitTexture, 0, 0, 0, 0, 0, requiredWidth, requiredHeight);
		((GuiGraphicsInvoker) graphics).invokeSubmitColoredRectangle(SkyblockerRenderPipelines.BLURRED_RECTANGLE, TextureSetup.singleTexture(blitTextureView), x0, y0, x1, y1, vertexColour, null);
	}

	public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	// Temp fix for this?
	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	public static void close() {
		BLIT_TEXTURE_POOL.close();
	}
}
