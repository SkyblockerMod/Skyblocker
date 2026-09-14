package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;


public class CritterCaptureFilter extends SimpleChatFilter {
	public CritterCaptureFilter() {
		super("^(CAPTURE|LOOT SHARE)! .*$");
	}

	@Override
	public ChatFilterResult state() {
		return Utils.isInSafari() ? SkyblockerConfigManager.get().chat.hideCritterCapture : ChatFilterResult.PASS;
	}
}
