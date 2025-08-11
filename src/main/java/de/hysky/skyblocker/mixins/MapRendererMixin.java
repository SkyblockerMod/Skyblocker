package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@ModifyExpressionValue(method = "createDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapDecoration;isAlwaysRendered()Z"))
	private boolean preventDecorationInDungeons(boolean alwaysRendered, @Local(argsOnly = true) MapDecoration decoration) {
		// Allow alwaysRendered if
		// 1. not in dungeons and map is disabled OR
		// 2. the decoration type is frame (self player) and don't show self head OR
		// 3. the decoration type is blue marker (other player) and the fancy map is off
		if (Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap) {
			MapDecorationType decorationType = decoration.type().value();
			boolean shouldShowSelfMarker = decorationType.equals(MapDecorationTypes.FRAME.value()) && !SkyblockerConfigManager.get().dungeons.dungeonMap.showSelfHead;
			boolean shouldShowOtherPlayerMarkers = decorationType.equals(MapDecorationTypes.BLUE_MARKER.value()) && !SkyblockerConfigManager.get().dungeons.dungeonMap.fancyMap;

			return shouldShowSelfMarker || shouldShowOtherPlayerMarkers;
		}

		return alwaysRendered;
	}
}
