package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import de.hysky.skyblocker.compatibility.CaxtonCompatibility;
import de.hysky.skyblocker.compatibility.ModernUICompatibility;
import de.hysky.skyblocker.mixins.accessors.GuiGraphicsExtractorInvoker;
import de.hysky.skyblocker.utils.render.state.gui.GuiCustomShapeRenderState;
import de.hysky.skyblocker.utils.render.state.gui.GuiEquipmentRenderState;
import de.hysky.skyblocker.utils.render.state.gui.GuiHorizontalGradientRenderState;
import de.hysky.skyblocker.utils.render.state.gui.GuiOutlinedTextRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
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

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public class GuiHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	/**
	 * Suitable for rendering two blurred rectangles at once
	 */
	private static final TexturePool BLIT_TEXTURE_POOL = TexturePool.create("Blit Pool", 4, GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST, TextureFormat.RGBA8);
	private static int blitIndexForFrame = -1;

	public static void nineSliceColored(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, int argb) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, argb);
	}

	public static void nineSliceColored(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, Color color) {
		nineSliceColored(graphics, texture, x, y, width, height, ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
	}

	public static void horizontalGradient(GuiGraphicsExtractor graphics, float startX, float startY, float endX, float endY, int colorStart, int colorEnd) {
		graphics.guiRenderState.addGuiElement(new GuiHorizontalGradientRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), (int) startX, (int) startY, (int) endX, (int) endY, colorStart, colorEnd, graphics.scissorStack.peek()));
	}

	public static void border(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y + 1, x + 1, y + height - 1, color);
		context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/**
	 * Extracts shape with given vertices. Note the vertices must be in the right or for this to work properly this function does not sort them for you.
	 * @param graphics gui graphics extractor
	 * @param vertices vertices of shape
	 * @param color color of shape
	 */
	public static void customShape(GuiGraphicsExtractor graphics, List<Vector2f> vertices, int color) {
		graphics.guiRenderState.addGuiElement(new GuiCustomShapeRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), vertices, color, graphics.scissorStack.peek()));
	}

	/**
	 * Draws a player head without blocking or a default head if profile is not available immediately.
	 * This fetches the profile so it will be available for future calls to this method.
	 */
	public static void playerHead(GuiGraphicsExtractor graphics, int x, int y, int size, UUID uuid) {
		PlayerSkin texture = CLIENT.playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(uuid))
				.getNow(Optional.empty())
				.map(PlayerSkinRenderCache.RenderInfo::playerSkin)
				.orElseGet(() -> DefaultPlayerSkin.get(uuid));
		PlayerFaceExtractor.extractRenderState(graphics, texture, x, y, size);
	}

	public static <S> void equipment(GuiGraphicsExtractor graphics, EquipmentLayerRenderer equipmentRenderer, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> assetKey, Model<S> model, S state, ItemStack stack, int x1, int y1, int x2, int y2, float rotation, float scale, float offset) {
		GuiEquipmentRenderState<S> renderState = new GuiEquipmentRenderState<>(equipmentRenderer, layerType, assetKey, model, state, stack, x1, y1, x2, y2, rotation, scale, offset, graphics.scissorStack.peek());

		graphics.guiRenderState.addPicturesInPictureState(renderState);
	}

	public static void outlinedText(GuiGraphicsExtractor graphics, Component text, int x, int y, int color, int outlineColor) {
		FormattedCharSequence orderedText = text.getVisualOrderText();
		outlinedText(graphics, orderedText, ModernUICompatibility.MODERNUI_ENABLED ? Component.literal(text.getString()).getVisualOrderText() : orderedText, x, y, color, outlineColor);
	}

	public static void outlinedText(GuiGraphicsExtractor graphics, FormattedCharSequence text, int x, int y, int color, int outlineColor) {
		outlinedText(graphics, text, text, x, y, color, outlineColor);
	}

	private static void outlinedText(GuiGraphicsExtractor graphics, FormattedCharSequence text, FormattedCharSequence outlineText, int x, int y, int color, int outlineColor) {
		if (CaxtonCompatibility.drawOutlinedText(graphics, text, x, y, color, outlineColor)) return;
		if (ModernUICompatibility.extractOutlinedText(graphics, text, outlineText, x, y, color, outlineColor)) return;

		GuiOutlinedTextRenderState renderState = new GuiOutlinedTextRenderState(CLIENT.font, text, new Matrix3x2f(graphics.pose()), x, y, color, outlineColor, false, false, graphics.scissorStack.peek());
		graphics.guiRenderState.addText(renderState);
	}

	/**
	 * Extracts a blurred rectangle to be rendered at the given position.
	 *
	 * @param radius The strength of the blur, must be positive.
	 */
	public static void blurredRectangle(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int radius) {
		if (blitIndexForFrame == -1) {
			RenderTarget mainRenderTarget = CLIENT.getMainRenderTarget();
			int requiredWidth = mainRenderTarget.width;
			int requiredHeight = mainRenderTarget.height;
			blitIndexForFrame = BLIT_TEXTURE_POOL.getNextAvailableIndex(requiredWidth, requiredHeight);
		}

		GpuTextureView blitTextureView = BLIT_TEXTURE_POOL.getTextureView(blitIndexForFrame);
		// The sampler needs to be linear in order for the shader sampling interpolation trick to work properly
		GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		// Pass the radius through the vertex colour - least painful way to do this
		int vertexColour = ARGB.color(radius, 255, 255);

		((GuiGraphicsExtractorInvoker) graphics).invokeInnerFill(SkyblockerRenderPipelines.BLURRED_RECTANGLE, TextureSetup.singleTexture(blitTextureView, sampler), x0, y0, x1, y1, vertexColour, null);
	}

	public static void updateScreenBlitTexture() {
		if (blitIndexForFrame != -1) {
			RenderTarget mainRenderTarget = CLIENT.getMainRenderTarget();
			int requiredWidth = mainRenderTarget.width;
			int requiredHeight = mainRenderTarget.height;
			GpuTextureView blitTextureView = BLIT_TEXTURE_POOL.getTextureView(blitIndexForFrame);

			// Copy the main render target colour texture to our temporary one since you cannot read from and write to the same texture in a single draw.
			RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(mainRenderTarget.getColorTexture(), blitTextureView.texture(), 0, 0, 0, 0, 0, requiredWidth, requiredHeight);
			blitIndexForFrame = -1;
		}
	}

	public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	// 1.21.10 Port: Temp fix for this?
	// 1.21.11 Port: "nothing is more permanent than a temporary solution"
	// 26.1 Port: still holds true!
	public static boolean hasShiftDown() {
		return Minecraft.getInstance().hasShiftDown();
	}

	public static void close() {
		BLIT_TEXTURE_POOL.close();
	}
}
