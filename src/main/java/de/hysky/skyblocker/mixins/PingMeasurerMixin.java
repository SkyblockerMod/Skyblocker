package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PingDebugMonitor.class)
public class PingMeasurerMixin {

	@ModifyArg(method = "onPongReceived", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/debugchart/LocalSampleLogger;push(J)V"))
	private long skyblocker$onPingResult(long ping) {
		if (Utils.isInCrimson()) {
			DojoManager.onPingResult(ping);
		}
		PredictiveSmoothAOTE.updatePing(ping);

		return ping;
	}
}
