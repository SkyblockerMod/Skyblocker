package de.hysky.skyblocker.utils;

import net.minecraft.client.input.MouseButtonInfo;

/// Contains utilities for working with containers.
public class ContainerUtils {

	/// Converts the {@code mouseButton} into the corresponding container click button.
	public static int getContainerClickButton(@MouseButtonInfo.MouseButton int mouseButton) {
		return mouseButton;
	}
}
