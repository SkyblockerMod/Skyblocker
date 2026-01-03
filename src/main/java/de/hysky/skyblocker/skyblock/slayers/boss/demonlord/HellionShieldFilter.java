package de.hysky.skyblocker.skyblock.slayers.boss.demonlord;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.chat.filters.SimpleChatFilter;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class HellionShieldFilter extends SimpleChatFilter {
	public HellionShieldFilter() {
		super("^(Your hit was reduced by Hellion Shield!|Strike using the \\w+ attunement on your dagger!)$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().slayers.blazeSlayer.hideHellionShield;
	}
}
