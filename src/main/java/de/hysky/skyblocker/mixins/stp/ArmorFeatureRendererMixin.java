package de.hysky.skyblocker.mixins.stp;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.stp.SkyblockerArmorTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorMaterial;layers()Ljava/util/List;"))
	private List<ArmorMaterial.Layer> skyblocker$modifyArmorLayers(List<ArmorMaterial.Layer> original, @Local ItemStack stack) {
		if (Utils.isOnHypixel()) {
			List<ArmorMaterial.Layer> customLayers = SkyblockerArmorTextures.getCustomArmorTextureLayers(stack);

			if (customLayers != SkyblockerArmorTextures.NO_CUSTOM_TEXTURES) return customLayers;
		}

		return original;
	}
}
