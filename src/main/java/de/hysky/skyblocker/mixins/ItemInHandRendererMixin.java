package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@ModifyReturnValue(method = "shouldInstantlyReplaceVisibleItem", at = @At("RETURN"))
	private boolean skyblocker$cancelComponentUpdateAnimation(boolean original, ItemStack from, ItemStack to) {
		return Utils.isOnSkyblock() && from.getItem() == to.getItem() ? original || SkyblockerConfigManager.get().uiAndVisuals.cancelComponentUpdateAnimation : original;
	}
}
