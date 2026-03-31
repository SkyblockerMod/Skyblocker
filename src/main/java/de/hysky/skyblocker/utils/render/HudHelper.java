package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import de.hysky.skyblocker.compatibility.CaxtonCompatibility;
import de.hysky.skyblocker.compatibility.ModernUICompatibility;
import de.hysky.skyblocker.mixins.accessors.GuiGraphicsExtractorInvoker;
import de.hysky.skyblocker.utils.render.gui.state.CustomShapeGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.HorizontalGradientGuiElementRenderState;
import de.hysky.skyblocker.utils.render.gui.state.OutlinedTextGuiElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

	public static void renderNineSliceColored(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, int argb) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, argb);
	}

	public static void renderNineSliceColored(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, Color color) {
		renderNineSliceColored(graphics, texture, x, y, width, height, ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
	}

	public static void drawHorizontalGradient(GuiGraphicsExtractor graphics, float startX, float startY, float endX, float endY, int colorStart, int colorEnd) {
		graphics.guiRenderState.submitGuiElement(new HorizontalGradientGuiElementRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), (int) startX, (int) startY, (int) endX, (int) endY, colorStart, colorEnd, graphics.scissorStack.peek()));
	}

	public static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y + 1, x + 1, y + height - 1, color);
		graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/**
	 * Draws shape with given vertices. Note the vertices must be in the right or for this to work properly this function does not sort them for you.
	 * @param context draw context
	 * @param vertices vertices of shape
	 * @param color color of shape
	 */
	public static void drawCustomShape(GuiGraphicsExtractor graphics, List<Vector2f> vertices, int color) {
		graphics.guiRenderState.submitGuiElement(new CustomShapeGuiElementRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), vertices, color, graphics.scissorStack.peek()));
	}

	/**
	 * Draws a player head without blocking or a default head if profile is not available immediately.
	 * This fetches the profile so it will be available for future calls to this method.
	 */
	public static void drawPlayerHead(GuiGraphicsExtractor graphics, int x, int y, int size, UUID uuid) {
		PlayerSkin texture = CLIENT.playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(uuid))
				.getNow(Optional.empty())
				.map(PlayerSkinRenderCache.RenderInfo::playerSkin)
				.orElseGet(() -> DefaultPlayerSkin.get(uuid));
		PlayerFaceRenderer.draw(graphics, texture, x, y, size);
	}

	public static <S> void drawEquipment(GuiGraphicsExtractor graphics, EquipmentLayerRenderer equipmentRenderer, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> assetKey, Model<S> model, S state, ItemStack stack, int x1, int y1, int x2, int y2, float rotation, float scale, float offset) {
		EquipmentGuiElementRenderState<S> renderState = new EquipmentGuiElementRenderState<>(equipmentRenderer, layerType, assetKey, model, state, stack, x1, y1, x2, y2, rotation, scale, offset, graphics.scissorStack.peek());

		graphics.guiRenderState.submitPicturesInPictureState(renderState);
	}

	public static void drawOutlinedText(GuiGraphicsExtractor graphics, Component text, int x, int y, int color, int outlineColor) {
		FormattedCharSequence orderedText = text.getVisualOrderText();
		drawOutlinedText(graphics, orderedText, ModernUICompatibility.MODERNUI_ENABLED ? Component.literal(text.getString()).getVisualOrderText() : orderedText, x, y, color, outlineColor);
	}

	public static void drawOutlinedText(GuiGraphicsExtractor graphics, FormattedCharSequence text, int x, int y, int color, int outlineColor) {
		drawOutlinedText(graphics, text, text, x, y, color, outlineColor);
	}

	private static void drawOutlinedText(GuiGraphicsExtractor graphics, FormattedCharSequence text, FormattedCharSequence outlineText, int x, int y, int color, int outlineColor) {
		if (CaxtonCompatibility.drawOutlinedText(graphics, text, x, y, color, outlineColor)) return;
		if (ModernUICompatibility.drawOutlinedText(graphics, text, outlineText, x, y, color, outlineColor)) return;

		OutlinedTextGuiElementRenderState renderState = new OutlinedTextGuiElementRenderState(CLIENT.font, text, new Matrix3x2f(graphics.pose()), x, y, color, outlineColor, false, false, graphics.scissorStack.peek());
		graphics.guiRenderState.submitText(renderState);
	}

	/**
	 * Submits a blurred rectangle to be rendered at the given position.
	 *
	 * @param radius The strength of the blur, must be positive.
	 */
	public static void submitBlurredRectangle(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int radius) {
		RenderTarget mainRenderTarget = CLIENT.getMainRenderTarget();
		int requiredWidth = mainRenderTarget.width;
		int requiredHeight = mainRenderTarget.height;

		int index = BLIT_TEXTURE_POOL.getNextAvailableIndex(requiredWidth, requiredHeight);
		GpuTexture blitTexture = BLIT_TEXTURE_POOL.getTexture(index);
		GpuTextureView blitTextureView = BLIT_TEXTURE_POOL.getTextureView(index);
		// The sampler needs to be linear in order for the shader sampling interpolation trick to work properly
		GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		// Pass the radius through the vertex colour - least painful way to do this
		int vertexColour = ARGB.color(radius, 255, 255);

		// Copy the main render target colour texture to our temporary one since you cannot read from and write to the same texture in a single draw.
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(mainRenderTarget.getColorTexture(), blitTexture, 0, 0, 0, 0, 0, requiredWidth, requiredHeight);
		((GuiGraphicsExtractorInvoker) graphics).invokeSubmitColoredRectangle(SkyblockerRenderPipelines.BLURRED_RECTANGLE, TextureSetup.singleTexture(blitTextureView, sampler), x0, y0, x1, y1, vertexColour, null);
	}

	public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	// 1.21.10 Port: Temp fix for this?
	// 1.21.11 Port: "nothing is more permanent than a temporary solution"
	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	public static void close() {
		BLIT_TEXTURE_POOL.close();
	}
}
