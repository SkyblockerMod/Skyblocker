package de.hysky.skyblocker.injected;

import java.util.Optional;

import net.minecraft.client.renderer.rendertype.RenderType;

public interface GlowRenderTypeHolder {

	default Optional<RenderType> skyblocker$getGlowRenderType() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
