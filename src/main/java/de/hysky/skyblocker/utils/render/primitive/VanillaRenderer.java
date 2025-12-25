package de.hysky.skyblocker.utils.render.primitive;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;

public interface VanillaRenderer<S> {

	void submitVanilla(S state, LevelRenderState worldState, SubmitNodeCollector commandQueue);
}
