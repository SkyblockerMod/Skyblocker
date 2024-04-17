package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "setScaleFactor", at = @At("TAIL"))
    public void onScaleFactorChange(double scaleFactor, CallbackInfo ci) {
        FancyStatusBars.updatePositions();
    }
}
