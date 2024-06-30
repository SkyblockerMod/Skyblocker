package de.hysky.skyblocker.mixins.stp;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.stp.SkyblockerArmorTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

	@ModifyExpressionValue(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;hasModel(Lnet/minecraft/component/type/EquippableComponent;Lnet/minecraft/entity/EquipmentSlot;)Z"))
	private boolean skyblocker$customEquipmentModelCheck(boolean hasEquipmentModel, @Local(argsOnly = true) ItemStack stack) {
		return hasEquipmentModel || (isCustomArmorEnabled() && SkyblockerArmorTextures.getCustomArmorModel(stack) != null);
	}

	//Note: This has to be a wrap operation (and not a modify var/expr) or else the orElseThrow could fail and cause a crash
	@WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseThrow()Ljava/lang/Object;", remap = false))
	private Object skyblocker$modifyArmorLayers(Optional<Identifier> equipmentComponentModelId, Operation<Object> operation, @Local(argsOnly = true) ItemStack stack) {
		Identifier customEquipmentModel = isCustomArmorEnabled() ? SkyblockerArmorTextures.getCustomArmorModel(stack) : null;

		return customEquipmentModel != null ? customEquipmentModel : operation.call(equipmentComponentModelId);
	}

	@Unique
	private static boolean isCustomArmorEnabled() {
		return (Utils.isOnSkyblock() || SkyblockerConfigManager.get().debug.stpGlobal) && SkyblockerConfigManager.get().uiAndVisuals.skyblockerTexturePredicates.armorTextures;
	}
}
