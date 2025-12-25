package de.hysky.skyblocker.skyblock.barn;

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

public class CallTrevor extends ChatPatternListener {
	public CallTrevor() {
		super("^Return to the Trapper soon to get a new animal to hunt!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().otherLocations.barn.enableCallTrevorMessage ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Component message, Matcher matcher) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return false;

		MutableComponent callMessage = Constants.PREFIX.get().append(Component.translatable("skyblocker.config.otherLocations.barn.callTrevor.message"));
		callMessage.withStyle(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call trevor"))
						.withColor(ChatFormatting.AQUA)
		);
		player.displayClientMessage(callMessage, false);

		return false; // We do not actually want to filter the message.
	}
}
