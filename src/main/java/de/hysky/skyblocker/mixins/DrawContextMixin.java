package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
	@ModifyExpressionValue(method = "drawCooldownProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/ItemStack;F)F"))
	private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(argsOnly = true) ItemStack stack) {
		return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
	}
}
