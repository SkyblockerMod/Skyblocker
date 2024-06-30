package de.hysky.skyblocker.mixins.stp;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import de.hysky.skyblocker.stp.SkyblockerArmorTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
	private Item skyblocker$customHeadTextures4Armor(Item original, @Local ItemStack stack, @Share("layers") LocalRef<List<ArmorMaterial.Layer>> layers) {
		if (original == Items.PLAYER_HEAD) {
			List<ArmorMaterial.Layer> customLayers = SkyblockerArmorTextures.getCustomArmorTextureLayers(stack);

			if (customLayers != SkyblockerArmorTextures.NO_CUSTOM_TEXTURES) {
				layers.set(customLayers);

				return Items.LEATHER_HELMET; //Pretend this is a leather helmet
			}
		}

		return original;
	}

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getSlotType()Lnet/minecraft/entity/EquipmentSlot;"))
	private EquipmentSlot skyblocker$returnHeadSlot4PlayerHead(EquipmentSlot original, @Share("layers") LocalRef<List<ArmorMaterial.Layer>> layers) {
		return layers.get() == null ? original : EquipmentSlot.HEAD;
	}

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorMaterial;layers()Ljava/util/List;"))
	private List<ArmorMaterial.Layer> skyblocker$modifyArmorLayers(List<ArmorMaterial.Layer> original, @Local ItemStack stack, @Share("layers") LocalRef<List<ArmorMaterial.Layer>> layers) {
		if (Utils.isOnHypixel()) {
			List<ArmorMaterial.Layer> customLayers = layers.get() == null ? SkyblockerArmorTextures.getCustomArmorTextureLayers(stack) : layers.get();

			if (customLayers != SkyblockerArmorTextures.NO_CUSTOM_TEXTURES) return customLayers;
		}

		return original;
	}

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasGlint()Z"))
	private boolean skyblocker$hideGlintOnCustomTexturedPlayerHeads(boolean original, @Share("layers") LocalRef<List<ArmorMaterial.Layer>> layers) {
		return original && layers.get() != null ? false : original;
	}
}
