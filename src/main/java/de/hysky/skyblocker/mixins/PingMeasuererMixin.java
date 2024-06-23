package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PingMeasurer.class)
public class PingMeasuererMixin {

    @WrapOperation(method = "onPingResult", at = @At(value = "INVOKE", target =  "Lnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;push(J)V"))
    private void skyblocker$onPingResult(MultiValueDebugSampleLogImpl log, long ping, Operation<Void> operation) {
        if (Utils.isInCrimson()) {
            DojoManager.onPingResult(ping);
        }
        operation.call(new Object[]{log, ping});
    }
}
