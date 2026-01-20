package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {
	@Shadow
	private LevelLoadTracker loadTracker;

	@Inject(method = { "render", "renderBackground" }, at = @At("HEAD"), cancellable = true)
	private void skyblocker$hideWorldLoadingScreen(CallbackInfo ci) {
		if (Utils.isOnHypixel() && this.loadTracker.statusView() == null) ci.cancel();
	}
}
