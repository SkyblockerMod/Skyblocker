package de.hysky.skyblocker.skyblock.barn;

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

public class CallTrevor extends ChatPatternListener {
	public CallTrevor() {
		super("^Return to the Trapper soon to get a new animal to hunt!$");
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().otherLocations.barn.enableCallTrevorMessage ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Text message, Matcher matcher) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return false;

		MutableText callMessage = Constants.PREFIX.get().append(Text.translatable("skyblocker.config.otherLocations.barn.callTrevor.message"));
		callMessage.styled(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call trevor"))
						.withColor(Formatting.AQUA)
		);
		player.sendMessage(callMessage, false);

		return false; // We do not actually want to filter the message.
	}
}
