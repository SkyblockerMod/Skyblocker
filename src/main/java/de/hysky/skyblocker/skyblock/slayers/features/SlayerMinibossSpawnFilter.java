package de.hysky.skyblocker.skyblock.slayers.features;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.chat.filters.SimpleChatFilter;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class SlayerMinibossSpawnFilter extends SimpleChatFilter {
	public SlayerMinibossSpawnFilter() {
		super("^SLAYER MINI-BOSS .+ has spawned!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().slayers.hideSlayerMinibossSpawn;
	}
}
