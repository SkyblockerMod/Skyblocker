package de.hysky.skyblocker.utils.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface Renderable {
	void render(WorldRenderContext context);
}
