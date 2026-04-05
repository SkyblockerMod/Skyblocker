package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import java.util.Objects;
import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AutopetFilter extends ChatPatternListener {
	public AutopetFilter() {
		super("^Autopet equipped your .*! VIEW RULE$");
	}

	@Override
	public boolean onMatch(Component _message, Matcher matcher) {
		if (SkyblockerConfigManager.get().chat.hideAutopet == ChatFilterResult.ACTION_BAR) {
			Objects.requireNonNull(Minecraft.getInstance().player).sendOverlayMessage(
					Component.literal(
							_message.getString().replace("VIEW RULE", "")
					));
		}
		return true;
	}

	@Override
	public ChatFilterResult state() {
		if (SkyblockerConfigManager.get().chat.hideAutopet == ChatFilterResult.ACTION_BAR)
			return ChatFilterResult.FILTER;
		else
			return SkyblockerConfigManager.get().chat.hideAutopet;
	}
}
