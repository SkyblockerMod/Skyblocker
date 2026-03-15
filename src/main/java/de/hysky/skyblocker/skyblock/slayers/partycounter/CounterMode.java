package de.hysky.skyblocker.skyblock.slayers.partycounter;

import net.minecraft.client.resources.language.I18n;

public enum CounterMode {
	AUTO,
	MANUAL;

	@Override
	public String toString() {
		return I18n.get("skyblocker.config.slayer.partySlayerCounter.mode." + name());
	}
}
