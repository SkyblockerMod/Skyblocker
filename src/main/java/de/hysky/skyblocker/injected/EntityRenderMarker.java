package de.hysky.skyblocker.injected;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.render.entity.state.EntityRenderState;

public interface EntityRenderMarker {

	default @Nullable EntityRenderState skyblocker$getEntityStateBeingRendered() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
