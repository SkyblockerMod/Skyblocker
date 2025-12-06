package de.hysky.skyblocker.utils.chat;

import net.minecraft.client.resource.language.I18n;

public enum ChatFilterResult {
	// Skip this one / no action
	PASS,
	// Filter
	FILTER,
	// Move to action bar
	ACTION_BAR;
	// Skip remaining checks, don't filter
	// null

	@Override
	public String toString() {
		return I18n.translate("skyblocker.config.chat.filter.chatFilterResult." + name());
	}
}
