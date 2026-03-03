package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMapTexture;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Inject(method = "close", at = @At("TAIL"))
	private void skyblocker$onGameRendererClose(CallbackInfo ci) {
		Renderer.close();
		GlowRenderer.getInstance().close();
		HudHelper.close();
		DungeonMapTexture.close();
	}

	@ModifyReturnValue(method = "getNightVisionScale", at = @At("RETURN"))
	private static float onGetNightVisionStrength(float original) {
		if (original == 1.0F && Utils.isOnSkyblock()) {
			var strength = SkyblockerConfigManager.get().uiAndVisuals.nightVisionStrength;
			if (strength == 0.0F) return 0.0F;
			return Math.clamp(strength / 100.0F, 0, 1);
		}
		return original;
	}
}
