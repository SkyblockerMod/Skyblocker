package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import de.hysky.skyblocker.injected.CustomGlowState;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(value = { OrderedRenderCommandQueueImpl.ItemCommand.class, OrderedRenderCommandQueueImpl.ModelCommand.class, OrderedRenderCommandQueueImpl.ModelPartCommand.class })
public class RenderCommandMixin implements CustomGlowState {
	@Unique
	private boolean hasCustomGlow = false;

	@Override
	public void skyblocker$markCustomGlow() {
		this.hasCustomGlow = true;
	}

	@Override
	public boolean skyblocker$hasCustomGlow() {
		return this.hasCustomGlow;
	}
}
