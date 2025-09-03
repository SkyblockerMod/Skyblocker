package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.regex.Matcher;

public class CallBadSignal extends ChatPatternListener {
	private static final Logger LOGGER = LogUtils.getLogger();

	public CallBadSignal() {
		// Match Fred with Abiphone mark
		super("^\\[NPC] (Fred): ✆ .*$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().mining.autoRedialOnBadSignal ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Text message, Matcher matcher) {
		// Obfuscated positions are randomly generated which can not be exactly matched
		if (!message.getString().contains("§k")) return false;

		String name = matcher.group(1);
		LOGGER.info("[Skyblocker Auto Redial] Automatically redialing <{}> due to bad signal.", name);
		MessageScheduler.INSTANCE.sendMessageAfterCooldown("/call " + matcher.group(1), true);
		return false;
	}
}
