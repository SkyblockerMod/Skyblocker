package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE;
import de.hysky.skyblocker.skyblock.teleport.ResponsiveSmoothAOTE;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyReturnValue(method = "getPos", at = @At("RETURN"))
    private Vec3d skyblocker$onCameraUpdate(Vec3d original) {
		if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.predictive){
			Vec3d pos = PredictiveSmoothAOTE.getInterpolatedPos();
			if (pos != null) {
				return pos;
			}
		} else {
			Vec3d pos = ResponsiveSmoothAOTE.getInterpolatedPos();
			if (pos != null) {
				return pos;
			}
		}



		return original;
    }
}
