package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import net.azureaaron.hmapi.utils.Utils;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.ItemStack;

@Mixin(ModelOverrideList.class)
public class ModelOverrideListMixin {

	@ModifyArg(method = "<init>(Lnet/minecraft/client/render/model/Baker;Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
	private Object skyblocker$injectCompiledPredicates(Object element, @Local ModelOverride override) {
		ModelOverrideList.BakedOverride bakedOverride = (ModelOverrideList.BakedOverride) element;

		//Copy predicates to the baked override
		bakedOverride.setItemPredicates(override.getItemPredicates());

		return element;
	}

	@ModifyExpressionValue(method = "getModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/ModelOverrideList$BakedOverride;test([F)Z"))
	private boolean skyblocker$applyPredicates(boolean original, @Local ModelOverrideList.BakedOverride bakedOverride, @Local(argsOnly = true) ItemStack stack) {
		UIAndVisualsConfig.STP config = SkyblockerConfigManager.get().uiAndVisuals.skyblockerTexturePredicates;

		if (config.skyblockItemTextures || (config.universalItemTextures && Utils.isOnHypixel()) || SkyblockerConfigManager.get().debug.stpGlobal) {
			SkyblockerTexturePredicate[] predicates = bakedOverride.getItemPredicates();

			if (predicates != null) {
				for (SkyblockerTexturePredicate predicate : predicates) {
					if (!predicate.test(stack)) return false;
				}

				return true;
			}
		}

		return original;
	}
}
