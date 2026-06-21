package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.skyblock.garden.LowerSensitivity;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Final
	@Shadow
	private Minecraft minecraft;

	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
	public Object skyblocker$gardenMouseLock(Object original) {
		if (LowerSensitivity.isSensitivityLowered())
			return -1 / 3d;
		else return original;

	}

	@Inject(method = "releaseMouse", at = @At("HEAD"), cancellable = true)
	private void skyblocker$keepCursorGrabbedDuringTransfer(CallbackInfo ci) {
		// Keep the cursor hidden through Hypixel's transfer screens so the transfer looks seamless
		if (this.minecraft.isWindowActive() && (this.minecraft.screen instanceof LevelLoadingScreen || this.minecraft.screen instanceof ServerReconfigScreen) && Utils.isOnHypixel()) ci.cancel();
	}

	@Inject(method = "grabMouse", at = @At("HEAD"))
	private void skyblocker$resetTransferState(CallbackInfo ci) {
		// The transfer is over once we're back in the game
		Utils.setTransferInterrupted(false);
	}
}
