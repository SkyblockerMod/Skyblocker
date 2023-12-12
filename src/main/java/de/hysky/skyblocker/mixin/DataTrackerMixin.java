package de.hysky.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.data.DataTracker;

@Mixin(DataTracker.class)
public class DataTrackerMixin {

	@WrapOperation(method = "writeUpdatedEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;copyToFrom(Lnet/minecraft/entity/data/DataTracker$Entry;Lnet/minecraft/entity/data/DataTracker$SerializedEntry;)V"))
	public void skyblocker$ignoreInvalidDataExceptions(DataTracker dataTracker, DataTracker.Entry<?> to, DataTracker.SerializedEntry<?> from, Operation<Void> operation) {
		if (Utils.isOnHypixel()) {
			try {
				operation.call(dataTracker, to, from);
			} catch (IllegalStateException ignored) {} //These exceptions cause annoying small lag spikes for some reason
		} else {
			operation.call(dataTracker, to, from);
		}
	}
}
