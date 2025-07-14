package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@ModifyExpressionValue(method = "createDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapDecoration;isAlwaysRendered()Z"))
	private boolean preventDecorationInDungeons(boolean alwaysRendered, @Local(argsOnly = true) MapDecoration decoration) {
		// Allow alwaysRendered if
		// 1. not in dungeons OR
		// 2. the decoration type is frame (self player) and don't show self head
		return (!Utils.isInDungeons() || decoration.type().value().equals(MapDecorationTypes.FRAME.value()) && !SkyblockerConfigManager.get().dungeons.dungeonMap.showSelfHead) && alwaysRendered;
	}
}
