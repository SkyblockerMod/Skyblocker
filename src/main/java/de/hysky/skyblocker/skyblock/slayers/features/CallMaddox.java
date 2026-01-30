package de.hysky.skyblocker.skyblock.slayers.features;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

public class CallMaddox {
	private static void sendMessage() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		Component text = Constants.PREFIX.get().append(Component.translatable("skyblocker.slayer.callMaddox.message")).withStyle(style ->
				style.withClickEvent(new ClickEvent.RunCommand("/call slayer"))
						.withColor(ChatFormatting.AQUA)
		);

		player.displayClientMessage(text, false);
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
