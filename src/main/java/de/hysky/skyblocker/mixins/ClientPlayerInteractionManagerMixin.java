package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.SwingAnimation;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

	@Inject(method = "method_41929", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", shift = At.Shift.AFTER))
	public void onInteractItem(Hand hand, PlayerEntity playerEntity, MutableObject<?> mutableObject,
							   int sequence, CallbackInfoReturnable<Packet<?>> cir, @Local ItemStack itemStack) {
		if (SkyblockerConfigManager.get().uiAndVisuals.swingOnAbilities
			&& SwingAnimation.hasAbility(itemStack)) {
			playerEntity.swingHand(hand);
		}
	}
}
