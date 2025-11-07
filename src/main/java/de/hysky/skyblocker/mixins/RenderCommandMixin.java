package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import de.hysky.skyblocker.injected.CustomGlowState;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(value = { OrderedRenderCommandQueueImpl.ItemCommand.class, OrderedRenderCommandQueueImpl.ModelCommand.class, OrderedRenderCommandQueueImpl.ModelPartCommand.class })
public class RenderCommandMixin implements CustomGlowState {
	@Unique
	private int customGlowColour = MobGlow.NO_GLOW;

	@Override
	public void skyblocker$setCustomGlowColour(int glowColour) {
		this.customGlowColour = glowColour;
	}

	@Override
	public int skyblocker$getCustomGlowColour() {
		return this.customGlowColour;
	}
}
