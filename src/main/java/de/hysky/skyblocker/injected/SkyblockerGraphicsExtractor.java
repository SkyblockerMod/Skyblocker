package de.hysky.skyblocker.injected;

import net.minecraft.client.gui.components.Renderable;

public interface SkyblockerGraphicsExtractor {
	default void skb$addDeferredElement(Renderable renderable) {
		throw new AssertionError("Implemented in Mixin");
	}
}
