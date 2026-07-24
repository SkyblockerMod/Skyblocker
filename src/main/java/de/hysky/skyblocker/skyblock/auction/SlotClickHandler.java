package de.hysky.skyblocker.skyblock.auction;

import com.mojang.blaze3d.platform.InputConstants;

@FunctionalInterface
public interface SlotClickHandler {

	void click(int slot, int button);

	default void click(int slot) {
		click(slot, InputConstants.MOUSE_BUTTON_LEFT);
	}
}
