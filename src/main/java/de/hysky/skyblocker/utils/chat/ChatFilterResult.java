package de.hysky.skyblocker.utils.chat;

import de.hysky.skyblocker.annotations.EnumDisabledValue;
import net.minecraft.client.resources.language.I18n;

public enum ChatFilterResult {
	// Skip this one / no action
	@EnumDisabledValue
	PASS,
	// Filter
	FILTER,
	// Move to action bar
	ACTION_BAR,
	// Show a toast
	TOAST;
	// Skip remaining checks, don't filter
	// null

	@Override
	public String toString() {
		return I18n.get("skyblocker.config.chat.filter.chatFilterResult." + name());
	}
}
