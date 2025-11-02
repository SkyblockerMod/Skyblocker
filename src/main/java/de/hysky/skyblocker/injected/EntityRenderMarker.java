package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.entity.state.EntityRenderState;

public interface EntityRenderMarker {

	@Nullable
	default EntityRenderState skyblocker$getEntityStateBeingRendered() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
