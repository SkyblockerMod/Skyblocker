package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ServerTransferHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;

@Mixin(Screen.class)
public class ScreenMixin {
	@Final
	@Shadow
	protected Minecraft minecraft;

	@Inject(method = "tick", at = @At("HEAD"))
	private void skyblocker$releaseCursorWhileUnfocused(CallbackInfo ci) {
		// If the window isn't focused while a transfer screen is up, give the cursor back so it isn't hidden when you come back
		if (((Object) this instanceof ServerReconfigScreen || (Object) this instanceof LevelLoadingScreen) && Utils.isOnHypixel() && !this.minecraft.isWindowActive()) {
			ServerTransferHelper.setInterrupted(true);
			if (this.minecraft.mouseHandler.isMouseGrabbed()) this.minecraft.mouseHandler.releaseMouse();
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void skyblocker$hideReconfiguringScreen(CallbackInfo ci) {
		if ((Object) this instanceof ServerReconfigScreen && Utils.isOnHypixel() && !ServerTransferHelper.isInterrupted()) ci.cancel();
	}
}
