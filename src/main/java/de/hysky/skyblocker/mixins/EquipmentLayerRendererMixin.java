package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

	@ModifyVariable(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at = @At("HEAD"), argsOnly = true)
	private ResourceKey<EquipmentAsset> customArmorModel(ResourceKey<EquipmentAsset> assetKey, @Local(argsOnly = true) ItemStack stack) {
		if (Utils.isOnSkyblock() && !stack.getUuid().isEmpty()) {
			Identifier identifier = SkyblockerConfigManager.get().general.customArmorModel.get(stack.getUuid());
			return identifier == null ? assetKey : ResourceKey.create(EquipmentAssets.ROOT_ID, identifier);
		}

		return assetKey;
	}
}
