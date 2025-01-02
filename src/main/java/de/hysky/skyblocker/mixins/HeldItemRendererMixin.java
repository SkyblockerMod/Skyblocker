package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

	@ModifyReturnValue(method = "shouldSkipHandAnimationOnSwap", at = @At("RETURN"))
	private boolean skyblocker$cancelComponentUpdateAnimation(boolean original, ItemStack from, ItemStack to) {
		return Utils.isOnSkyblock() && from.getItem() == to.getItem() ? original || SkyblockerConfigManager.get().uiAndVisuals.cancelComponentUpdateAnimation : original;
	}
}
