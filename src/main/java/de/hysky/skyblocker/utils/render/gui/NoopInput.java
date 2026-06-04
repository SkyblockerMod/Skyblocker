package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.input.InputWithModifiers;

public record NoopInput() implements InputWithModifiers {
	public static final InputWithModifiers INSTANCE = new NoopInput();

	@Override
	public int input() {
		return 0;
	}

	@Override
	public int modifiers() {
		return 0;
	}
}
