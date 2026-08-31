package de.hysky.skyblocker.mixins;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;

@Mixin(Window.class)
public class WindowMixin {
	@Inject(method = "setGuiScale", at = @At("TAIL"))
	public void skyblocker$onScaleFactorChange(CallbackInfo ci) {
		FancyStatusBars.INSTANCE.updatePositions(false);
	}
}
