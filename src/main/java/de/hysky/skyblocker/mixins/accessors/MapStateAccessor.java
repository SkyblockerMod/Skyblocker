package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MapState.class)
public interface MapStateAccessor {
	@Accessor
	Map<String, MapDecoration> getDecorations();
}
