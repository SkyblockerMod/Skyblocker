package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;

import de.hysky.skyblocker.injected.CustomGlowState;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(OrderedRenderCommandQueueImpl.ItemCommand.class)
public class ItemCommandMixin implements CustomGlowState {
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
