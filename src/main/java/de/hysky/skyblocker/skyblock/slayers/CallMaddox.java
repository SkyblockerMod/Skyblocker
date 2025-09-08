package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CallMaddox {
	private static void sendMessage() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;

		Text text = Constants.PREFIX.get().append(Text.translatable("skyblocker.slayer.callMaddox.message")).styled(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call slayer"))
						.withColor(Formatting.AQUA)
		);

		player.sendMessage(text, false);
	}

	// This is also called when the slayer is cancelled.
	public static void onSlayerFailed() {
		if (!SkyblockerConfigManager.get().slayers.callMaddox.sendMessageOnFail) return;
		if (Utils.isInTheRift()) return;
		sendMessage();
	}

	public static void onBossKilled() {
		if (!SkyblockerConfigManager.get().slayers.callMaddox.sendMessageOnKill) return;
		if (Utils.isInTheRift()) return;
		sendMessage();
	}
}
