package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

	@WrapMethod(
			method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V"
	)
	private void customArmorModel(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model model, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, @Nullable Identifier texture, Operation<Void> original) {
		if (!Utils.isOnSkyblock()) original.call(layerType, assetKey, model, stack, matrices, vertexConsumers, light, texture);
		String uuid = ItemUtils.getItemUuid(stack);
		if (uuid.isEmpty()) original.call(layerType, assetKey, model, stack, matrices, vertexConsumers, light, texture);
		Identifier identifier = SkyblockerConfigManager.get().general.customArmorModel.get(uuid);
		original.call(layerType, identifier == null ? assetKey : RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, identifier), model, stack, matrices, vertexConsumers, light, texture);
	}
}
