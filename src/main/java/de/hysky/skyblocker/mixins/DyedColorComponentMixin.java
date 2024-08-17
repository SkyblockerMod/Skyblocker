package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedColorComponent.class)
public class DyedColorComponentMixin {

	@ModifyReturnValue(method = "getColor", at = @At("RETURN"))
	private static int skyblocker$customDyeColor(int originalColor, @Local(argsOnly = true) ItemStack stack) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(stack);

			if (SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(itemUuid)) {
				return ColorHelper.Argb.fullAlpha(CustomArmorAnimatedDyes.animateColorTransition(SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid)));
			}

			return ColorHelper.Argb.fullAlpha(SkyblockerConfigManager.get().general.customDyeColors.getOrDefault(itemUuid, originalColor));
		}

		return originalColor;
	}
}
