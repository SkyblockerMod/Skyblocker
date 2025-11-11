package de.hysky.skyblocker.utils.render.primitive;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;

public interface VanillaRenderer<S> {

	void submitVanilla(S state, WorldRenderState worldState, OrderedRenderCommandQueue commandQueue);
}
