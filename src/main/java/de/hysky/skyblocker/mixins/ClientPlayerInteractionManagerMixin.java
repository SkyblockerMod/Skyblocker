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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	// Inject so that we only swing when the result is not success because vanilla handles swing on success.
	@Inject(method = "method_41929",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")),
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;")
	)
	public void swingOnAbility(Hand hand, PlayerEntity playerEntity, MutableObject<?> mutableObject,
							   int sequence, CallbackInfoReturnable<Packet<?>> cir, @Local(ordinal = 0) ItemStack itemStack) {
		if (SkyblockerConfigManager.get().uiAndVisuals.swingOnAbilities
				&& SwingAnimation.hasAbility(itemStack)) {
			swingHandWithoutPackets(playerEntity, hand);
		}
	}

	@Unique
	private void swingHandWithoutPackets(PlayerEntity playerEntity, Hand hand) {
		playerEntity.swingHand(hand, false); // The playerEntity override for swingHand is the other method with just the hand parameter, this one isn't overridden and doesn't lead to sending packets.
	}
}
