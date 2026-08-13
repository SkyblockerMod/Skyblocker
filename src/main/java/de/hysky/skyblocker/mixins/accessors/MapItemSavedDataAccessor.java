package de.hysky.skyblocker.mixins.accessors;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {
	@Accessor
	Map<String, MapDecoration> getDecorations();
}
