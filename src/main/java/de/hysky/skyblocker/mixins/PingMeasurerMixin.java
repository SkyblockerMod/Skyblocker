package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.network.PingMeasurer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PingMeasurer.class)
public class PingMeasurerMixin {

    @ModifyArg(method = "onPingResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;push(J)V"))
    private long skyblocker$onPingResult(long ping) {
        if (Utils.isInCrimson()) {
            DojoManager.onPingResult(ping);
        }

        return ping;
    }
}
