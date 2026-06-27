package de.hysky.skyblocker.mixins;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

	@ModifyExpressionValue(method = "prepareOutlineSubmit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderType;outline()Ljava/util/Optional;"))
	private Optional<RenderType> skyblocker$useCustomGlowRenderType(Optional<RenderType> original, ItemFeatureRenderer.Submit submit, @Local(name = "material") BakedQuad.MaterialInfo material) {
		if (submit.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW) {
			return material.itemRenderType().skyblocker$getGlowRenderType();
		}

		return original;
	}

	@ModifyExpressionValue(method = "prepareOutlineSubmit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit;outlineColor()I"))
	private int skyblocker$useCustomGlowColour(int original, ItemFeatureRenderer.Submit submit) {
		if (submit.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW) {
			return submit.skyblocker$getCustomGlowColour();
		}

		return original;
	}
}
