package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class SlayerMinibossSpawnFilter extends SimpleChatFilter {
	public SlayerMinibossSpawnFilter() {
		super("^SLAYER MINI-BOSS .+ has spawned!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideSlayerMinibossSpawn;
	}
}
