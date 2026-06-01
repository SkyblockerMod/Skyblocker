package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import de.hysky.skyblocker.injected.CustomGlowState;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;

@Mixin(value = { ItemFeatureRenderer.Submit.class })
public class RenderSubmitMixin implements CustomGlowState {
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
