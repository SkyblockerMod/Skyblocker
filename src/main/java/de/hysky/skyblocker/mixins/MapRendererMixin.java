package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@ModifyExpressionValue(method = "extractDecorationRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapDecoration;renderOnFrame()Z"))
	private boolean preventDecorationInDungeons(boolean alwaysRendered, @Local(argsOnly = true) MapDecoration decoration) {
		// Allow alwaysRendered if
		// 1. not in dungeons and map is disabled OR
		// 2. the decoration type is frame (self player) and either fancy map or show self head are off OR
		// 3. the decoration type is blue marker (other player) and the fancy map is off
		if (Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap) {
			MapDecorationType decorationType = decoration.type().value();
			boolean fancyMap = SkyblockerConfigManager.get().dungeons.dungeonMap.fancyMap;
			boolean shouldShowSelfMarker = decorationType.equals(MapDecorationTypes.FRAME.value()) && (!fancyMap || !SkyblockerConfigManager.get().dungeons.dungeonMap.showSelfHead);
			boolean shouldShowOtherPlayerMarkers = decorationType.equals(MapDecorationTypes.BLUE_MARKER.value()) && fancyMap;

			return shouldShowSelfMarker || shouldShowOtherPlayerMarkers;
		}

		return alwaysRendered;
	}
}
