package de.hysky.skyblocker.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.special.SpecialEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
	@Shadow
	private @Nullable ItemStack itemActivationItem;

	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyArg(method = "buildFireQuad", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;buildSpriteQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFFFI)V"))
	private static float configureFlameHeight1(float y) {
		return configureFlameHeightInternal(y);
	}

	@ModifyArg(method = "buildFireQuad", index = 6, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;buildSpriteQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFFFI)V"))
	private static float configureFlameHeight2(float y) {
		return configureFlameHeightInternal(y);
	}

	@Unique
	private static float configureFlameHeightInternal(float y) {
		return y - (0.5f - ((float) SkyblockerConfigManager.get().uiAndVisuals.flameOverlay.flameHeight / 200.0f));
	}

	@ModifyArg(method = "buildFireQuad", index = 8, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;buildSpriteQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFFFI)V"))
	private static int configureFlameOpacity(int colour) {
		float opacity = ARGB.alphaFloat(colour);
		float modifiedOpacity = opacity - (0.8f - ((float) SkyblockerConfigManager.get().uiAndVisuals.flameOverlay.flameOpacity / 125.0f));

		return ARGB.color(modifiedOpacity, colour);
	}

	@Inject(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
	private void skyblocker$renderSpecialEffectsItemName(PoseStack poseStack, float partialTicks, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
		// Render item names for all special effect items
		if (SpecialEffects.ITEM.get() != itemActivationItem) return;

		Component name = itemActivationItem.getHoverName();
		int x = -minecraft.font.width(name) / 2;
		poseStack.pushPose();
		poseStack.scale(-0.03125f, -0.03125f, 0.03125f);
		submitNodeCollector.submitText(poseStack, x, 16f, name.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, 0xF000F0, 0xFFFFFFFF, 0, 0);
		poseStack.popPose();
	}
}
