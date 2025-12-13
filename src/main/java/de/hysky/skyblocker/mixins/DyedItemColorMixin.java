package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedItemColor.class)
public class DyedItemColorMixin {

	@ModifyReturnValue(method = "getOrDefault", at = @At("RETURN"))
	private static int skyblocker$customDyeColor(int originalColor, @Local(argsOnly = true) ItemStack stack) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = stack.getUuid();

			if (SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(itemUuid)) {
				return ARGB.opaque(CustomArmorAnimatedDyes.animateColorTransition(SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid)));
			}

			if (SkyblockerConfigManager.get().general.customDyeColors.containsKey(itemUuid)) {
				return ARGB.opaque(SkyblockerConfigManager.get().general.customDyeColors.getInt(itemUuid));
			}
		}

		return originalColor;
	}
}
