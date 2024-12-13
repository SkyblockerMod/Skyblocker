package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@ModifyReturnValue(method = "getNightVisionStrength", at = @At("RETURN"))
	private static float onGetNightVisionStrength(float original) {
		if (original == 1.0F && Utils.isOnSkyblock()) {
			var strength = SkyblockerConfigManager.get().uiAndVisuals.nightVisionStrength;
			return Math.min(Math.max(strength == 0F ? 0F : strength / 100.0F, 0), 100);
		}
		return original;
	}
}
