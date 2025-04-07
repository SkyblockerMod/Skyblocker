package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(method = "getProjectileType", at = @At("HEAD"), cancellable = true)
	private void skyblocker$cancelShortbowPullAnimation(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		if (stack.isShortbow() && SkyblockerConfigManager.get().uiAndVisuals.cancelShortbowPullAnimation) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}
