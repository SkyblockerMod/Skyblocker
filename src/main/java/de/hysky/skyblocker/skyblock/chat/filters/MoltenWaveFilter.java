package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class MoltenWaveFilter extends SimpleChatFilter {
	public MoltenWaveFilter() {
		super("^Your Molten Wave hit " + NUMBER + " enem(?:y|ies) for " + NUMBER + " damage\\.$");
	}

	@Override
	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().chat.hideMoltenWave;
	}
}
