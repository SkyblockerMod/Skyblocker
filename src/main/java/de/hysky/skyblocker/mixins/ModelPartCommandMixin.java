package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;

import de.hysky.skyblocker.injected.CustomGlowState;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OrderedRenderCommandQueueImpl.ModelPartCommand.class)
public class ModelPartCommandMixin implements CustomGlowState {
	@Unique
	private boolean hasCustomGlow = false;

	@Override
	public void markCustomGlow() {
		this.hasCustomGlow = true;
	}

	@Override
	public boolean hasCustomGlow() {
		return this.hasCustomGlow;
	}
}
