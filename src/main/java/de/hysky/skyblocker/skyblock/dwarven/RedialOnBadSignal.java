package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;

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
	protected boolean onMatch(Text message, Matcher matcher) {
		// Obfuscated positions are randomly generated which can not be exactly matched
		if (!message.getString().contains("§k")) return false;

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return false;

		String name = matcher.group(1);
		MutableText callMessage = Constants.PREFIX.get().append(Text.translatable("skyblocker.config.mining.redialOnBadSignal.message", name));
		callMessage.styled(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call " + name))
						.withColor(Formatting.AQUA)
		);
		player.sendMessage(callMessage, false);

		return false;
	}
}
