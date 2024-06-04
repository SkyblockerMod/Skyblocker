package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PingMeasurer.class)
public class PingMeasureMixin {

    @Inject(method = "onPingResult", at = @At("RETURN"))
    private void skyblocker$onPingResult(PingResultS2CPacket packet, CallbackInfo ci) {
        if (Utils.isInCrimson()) {
            long ping = System.currentTimeMillis() - packet.startTime();
            DojoManager.onPingResult(ping);
        }
    }
}
