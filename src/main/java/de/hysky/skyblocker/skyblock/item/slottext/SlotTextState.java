package de.hysky.skyblocker.skyblock.item.slottext;

/**
 * Used in {@link SlotTextManager#isEnabled()} to determine whether the slot text should be shown or not.
 */
public enum SlotTextState {
	ENABLED,
	HOLD_TO_SHOW,
	PRESS_TO_TOGGLE,
	HOLD_TO_HIDE,
	DISABLED;

	@Override
	public String toString() {
		return switch (this) {
			case ENABLED         -> "Enabled";
			case HOLD_TO_SHOW    -> "Hold to Show";
			case PRESS_TO_TOGGLE -> "Press to Toggle";
			case HOLD_TO_HIDE    -> "Hold to Hide";
			case DISABLED        -> "Disabled";
		};
	}
}
