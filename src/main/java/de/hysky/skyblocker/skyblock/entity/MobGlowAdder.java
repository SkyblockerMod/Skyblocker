package de.hysky.skyblocker.skyblock.entity;

import net.minecraft.entity.Entity;

public abstract class MobGlowAdder {
	protected static final int NO_GLOW = MobGlow.NO_GLOW;

	protected MobGlowAdder() {
		MobGlow.registerGlowAdder(this);
	}

	/**
	 * Computes the glow colour of the {@code entity}.
	 *
	 * @return The glow colour of the entity or {@link #NO_GLOW}.
	 */
	public abstract int computeColour(Entity entity);

	/**
	 * @return If this adder is enabled.
	 */
	public abstract boolean isEnabled();
}
