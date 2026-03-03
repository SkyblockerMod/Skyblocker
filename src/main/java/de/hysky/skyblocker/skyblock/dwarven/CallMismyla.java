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

public class CallMismyla extends ChatPatternListener {
	public CallMismyla() {
		super("^([\\w' ]+) Commission Complete! Visit the King to claim your rewards!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().mining.callMismyla ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Component message, Matcher matcher) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return false;

		MutableComponent callMessage = Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.callMismyla.message"));
		callMessage.withStyle(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call mismyla"))
						.withColor(ChatFormatting.AQUA)
		);
		player.displayClientMessage(callMessage, false);

		return false;
	}
}
