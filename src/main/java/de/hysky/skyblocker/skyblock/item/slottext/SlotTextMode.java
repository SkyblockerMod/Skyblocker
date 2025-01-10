package de.hysky.skyblocker.skyblock.item.slottext;

import net.minecraft.client.resource.language.I18n;

/**
 * Used in {@link SlotTextManager#isEnabled()} to determine whether the slot text should be shown or not.
 */
public enum SlotTextMode {
	ENABLED,
	HOLD_TO_SHOW,
	PRESS_TO_TOGGLE,
	HOLD_TO_HIDE,
	DISABLED;

	@Override
	public String toString() {
		return I18n.translate("skyblocker.config.uiAndVisuals.slotText.mode." + name());
	}
}
