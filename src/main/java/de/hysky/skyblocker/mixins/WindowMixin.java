package de.hysky.skyblocker.mixins;

import com.mojang.blaze3d.platform.Window;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
	@Inject(method = "setGuiScale", at = @At("TAIL"))
	public void skyblocker$onScaleFactorChange(CallbackInfo ci) {
		FancyStatusBars.updatePositions(false);
	}
}
