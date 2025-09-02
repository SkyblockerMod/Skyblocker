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

public class CallMismyla extends ChatPatternListener {
	public CallMismyla() {
		super("^([\\w' ]+) Commission Complete! Visit the King to claim your rewards!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().mining.callMismyla ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Text message, Matcher matcher) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return false;

		MutableText callMessage = Constants.PREFIX.get().append(Text.translatable("skyblocker.config.mining.callMismyla.message"));
		callMessage.styled(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call mismyla"))
						.withColor(Formatting.AQUA)
		);
		player.sendMessage(callMessage, false);

		return false;
	}
}
