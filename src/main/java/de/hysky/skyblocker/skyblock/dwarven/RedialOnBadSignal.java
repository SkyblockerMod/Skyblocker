package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class RedialOnBadSignal extends ChatPatternListener {
	public RedialOnBadSignal() {
		// Match Fred with Abiphone mark
		super("^\\[NPC] (Fred): ✆ .*$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().mining.redialOnBadSignal ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Component message, Matcher matcher) {
		// Obfuscated positions are randomly generated which can not be exactly matched
		if (!message.getString().contains("§k")) return false;

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return false;

		String name = matcher.group(1);
		MutableComponent callMessage = Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.redialOnBadSignal.message", name));
		callMessage.withStyle(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call " + name))
						.withColor(ChatFormatting.AQUA)
		);
		player.displayClientMessage(callMessage, false);

		return false;
	}
}
