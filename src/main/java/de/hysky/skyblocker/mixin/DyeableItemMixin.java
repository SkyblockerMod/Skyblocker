package de.hysky.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyeableItem.class)
public interface DyeableItemMixin {
	@ModifyReturnValue(method = "getColor", at = @At("RETURN"))
	private int skyblocker$customDyeColor(int originalColor, ItemStack stack) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(stack);

			if (SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(itemUuid)) {
				return CustomArmorAnimatedDyes.animateColorTransition(SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid));
			}

			return SkyblockerConfigManager.get().general.customDyeColors.getOrDefault(itemUuid, originalColor);
		}

		return originalColor;
	}
}
