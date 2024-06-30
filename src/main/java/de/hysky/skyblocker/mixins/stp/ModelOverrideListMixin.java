package de.hysky.skyblocker.mixins.stp;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.ItemStack;

@Mixin(ModelOverrideList.class)
public class ModelOverrideListMixin {

	@WrapOperation(method = "<init>(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
	private boolean skyblocker$injectCompiledPredicates(List<Object> list, Object element, Operation<Boolean> operation, @Local ModelOverride override) {
		ModelOverrideList.BakedOverride bakedOverride = (ModelOverrideList.BakedOverride) element;

		//Copy predicates to the baked override
		bakedOverride.setItemPredicates(override.getItemPredicates());

		return operation.call(list, element);
	}

	@ModifyExpressionValue(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/ModelOverrideList$BakedOverride;test([F)Z"))
	private boolean skyblocker$applyPredicates(boolean original, @Local ModelOverrideList.BakedOverride bakedOverride, @Local(argsOnly = true) ItemStack stack) {
		if (Utils.isOnHypixel()) {
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
