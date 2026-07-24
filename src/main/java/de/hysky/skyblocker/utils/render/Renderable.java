package de.hysky.skyblocker.utils.render;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

public interface Renderable {
	void extractRendering(PrimitiveCollector collector);
}
