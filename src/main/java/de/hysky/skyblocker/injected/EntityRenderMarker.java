package de.hysky.skyblocker.injected;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;

public interface EntityRenderMarker {

	default @Nullable EntityRenderState skyblocker$getEntityStateBeingRendered() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
