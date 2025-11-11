package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

	@ModifyVariable(method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V", at = @At("HEAD"), argsOnly = true)
	private RegistryKey<EquipmentAsset> customArmorModel(RegistryKey<EquipmentAsset> assetKey, @Local(argsOnly = true) ItemStack stack) {
		if (Utils.isOnSkyblock() && !stack.getUuid().isEmpty()) {
			Identifier identifier = SkyblockerConfigManager.get().general.customArmorModel.get(stack.getUuid());
			return identifier == null ? assetKey : RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, identifier);
		}

		return assetKey;
	}
}
