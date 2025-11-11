package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.input.AbstractInput;

public record NoopInput() implements AbstractInput {
	public static final AbstractInput INSTANCE = new NoopInput();

	@Override
	public int getKeycode() {
		return 0;
	}

	@Override
	public int modifiers() {
		return 0;
	}
}
