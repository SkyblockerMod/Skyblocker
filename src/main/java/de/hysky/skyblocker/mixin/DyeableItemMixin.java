package de.hysky.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyeableItem.class)
public interface DyeableItemMixin {
	@ModifyReturnValue(method = "getColor", at = @At("RETURN"))
	private static int skyblocker$customDyeColor(int originalColor, ItemStack stack) {
		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(stack);

			return SkyblockerConfigManager.get().general.customDyeColors.getOrDefault(itemUuid, originalColor);
		}

		return originalColor;
	}
}
