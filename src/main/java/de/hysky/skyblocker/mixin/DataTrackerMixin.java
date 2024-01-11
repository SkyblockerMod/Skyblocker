package de.hysky.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.data.DataTracker;

@Mixin(DataTracker.class)
public class DataTrackerMixin {

	@Inject(method = "copyToFrom", at = @At(value = "NEW", target = "Ljava/lang/IllegalStateException;", shift = At.Shift.BEFORE), cancellable = true)
	public void skyblocker$ignoreInvalidDataExceptions(CallbackInfo ci) {
		//These exceptions cause annoying small lag spikes for some reason
		if (Utils.isOnHypixel()) ci.cancel();
	}
}
