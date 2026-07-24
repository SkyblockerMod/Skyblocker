package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

@Mixin(MapRenderer.class)
public interface MapRendererInvoker {

	@Invoker
	MapRenderState.MapDecorationRenderState invokeExtractDecorationRenderState(MapDecoration mapDecoration);
}
