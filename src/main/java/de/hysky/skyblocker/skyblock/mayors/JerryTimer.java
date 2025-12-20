package de.hysky.skyblocker.skyblock.mayors;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.sounds.SoundEvents;

public final class JerryTimer {
	private static boolean isJerryActive = false;

	private JerryTimer() {}

	@Init
	public static void init() {
		//Example message: "§b ☺ §eThere is a §aGreen Jerry§e!"
		//There are various formats, all of which start with the "§b ☺ " prefix and contain the word "<color> Jerry"
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (overlay || !isJerryActive || !SkyblockerConfigManager.get().helpers.jerry.enableJerryTimer) return true;
			String text = message.getString();
			//This part of hypixel still uses legacy text formatting, so we can't directly check for the actual text
			if (!text.startsWith("§b ☺ ") || !text.contains("Jerry")) return true;
			HoverEvent hoverEvent = message.getStyle().getHoverEvent();
			if (hoverEvent == null || hoverEvent.action() != HoverEvent.Action.SHOW_TEXT) return true;
			LocalPlayer player = Minecraft.getInstance().player;
			Scheduler.INSTANCE.schedule(() -> {
				if (player == null || !Utils.isOnSkyblock()) return;
				player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.helpers.jerry.sendJerryTimerMessage")).withStyle(ChatFormatting.GREEN), false);
				player.playSound(SoundEvents.VILLAGER_TRADE, 100f, 1.0f);
			}, 20 * 60 * 6); // 6 minutes

			return true;
		});

		SkyblockEvents.MAYOR_CHANGE.register(() -> isJerryActive = MayorUtils.getActivePerks().contains("Jerrypocalypse"));
	}
}
