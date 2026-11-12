package de.hysky.skyblocker.utils;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.input.MouseButtonInfo;

/// Contains utilities for working with containers.
public class ContainerUtils {

	/// Converts the {@code mouseButton} into the corresponding container click button.
	public static int getContainerClickButton(@MouseButtonInfo.MouseButton int mouseButton) {
		return switch (mouseButton) {
			case InputConstants.MOUSE_BUTTON_LEFT -> 0;
			case InputConstants.MOUSE_BUTTON_RIGHT -> 1;
			default -> mouseButton;
		};
	}

	/// Converts the {@code containerButton} into the corresponding mouse click button.
	public static int getMouseClickButton(int containerButton) {
		return switch (containerButton) {
			case 0 -> InputConstants.MOUSE_BUTTON_LEFT;
			case 1 -> InputConstants.MOUSE_BUTTON_RIGHT;
			default -> containerButton;
		};
	}
}
