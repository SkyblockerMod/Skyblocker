package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE;
import de.hysky.skyblocker.skyblock.teleport.ResponsiveSmoothAOTE;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

	@ModifyReturnValue(method = "getPosition", at = @At("RETURN"))
	private Vec3 skyblocker$onCameraUpdate(Vec3 original) {
		if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.predictive) {
			Vec3 pos = PredictiveSmoothAOTE.getInterpolatedPos();
			if (pos != null) {
				return pos;
			}
		} else {
			Vec3 pos = ResponsiveSmoothAOTE.getInterpolatedPos();
			if (pos != null) {
				return pos;
			}
		}



		return original;
	}
}
