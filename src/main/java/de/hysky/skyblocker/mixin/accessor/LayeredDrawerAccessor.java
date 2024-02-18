package de.hysky.skyblocker.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.LayeredDrawer;

@Mixin(LayeredDrawer.class)
public interface LayeredDrawerAccessor {

	@Accessor
	List<LayeredDrawer.Layer> getLayers();
}
