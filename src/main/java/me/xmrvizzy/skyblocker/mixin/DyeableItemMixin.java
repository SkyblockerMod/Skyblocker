package me.xmrvizzy.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

@Mixin(DyeableItem.class)
public interface DyeableItemMixin {

	@ModifyReturnValue(method = "getColor", at = @At("RETURN"))
	default public int skyblocker$customDyeColor(int originalColor, @Local ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		
		if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes")) {
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid =  extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;
			
			return SkyblockerConfig.get().general.customDyeColors.getOrDefault(itemUuid, originalColor);
		}
		
		return originalColor;
	}
}
