package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

	@Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
	private void beans(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		if (Utils.isOnSkyblock() && slotId == -1) {
			MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.literal("Slot -1 click prevented!"), Text.literal("Hypixel would have probably kicked you for that! Can you believe it?")));
			ci.cancel();
		}
	}
}
