package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.MapRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@ModifyExpressionValue(method = "draw", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/MapRenderState$Decoration;alwaysRendered:Z"))
	private boolean preventDecorationInDungeons(boolean alwaysRendered) {
		return !Utils.isInDungeons() && alwaysRendered;
	}
}
