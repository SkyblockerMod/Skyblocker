package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.config.SkyblockerConfigManager;

@Debug(export = true)
@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {

	@ModifyExpressionValue(
			method = {"appendItemLayers", "shouldPlaySwapAnimation"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"),
			order = 900
	)
	private Object modifyModel(Object o, @Local(argsOnly = true) ItemStack stack) {
		if (o instanceof Identifier && !stack.getUuid().isEmpty() && SkyblockerConfigManager.get().general.customItemModel.containsKey(stack.getUuid())) {
			return SkyblockerConfigManager.get().general.customItemModel.get(stack.getUuid());
		}
		return o;
	}
}
