package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.skyblock.slayers.boss.voidgloom.BeaconHighlighter;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {
	@Shadow
	@Final
	private SyncedDataHolder entity;

	@Inject(method = "assignValues", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValue(Lnet/minecraft/network/syncher/SynchedEntityData$DataItem;Lnet/minecraft/network/syncher/SynchedEntityData$DataValue;)V"))
	private <T> void skyblocker$onWriteUpdatedEntries(CallbackInfo ci, @Local(name = "dataItem") SynchedEntityData.DataItem<T> dataItem, @Local(name = "item") SynchedEntityData.DataValue<T> newValue) {
		BeaconHighlighter.onThrowBeacon(entity, dataItem, newValue);
	}

	@Inject(method = "assignValue", at = @At(value = "NEW", target = "Ljava/lang/IllegalStateException;"), cancellable = true)
	public void skyblocker$ignoreInvalidDataExceptions(CallbackInfo ci) {
		//These exceptions cause annoying small lag spikes for some reason
		if (Utils.isOnHypixel()) ci.cancel();
	}
}
