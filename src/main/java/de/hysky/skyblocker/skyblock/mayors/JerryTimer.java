package de.hysky.skyblocker.skyblock.mayors;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class JerryTimer {
	private JerryTimer() {
	}
	public static void init() {
		//Example message: "§b ☺ §eThere is a §aGreen Jerry§e!"
		//There are various formats, all of which start with the "§b ☺ " prefix and contain the word "<color> Jerry"
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay || !MayorUtils.getMayor().name().equals("Jerry") || !SkyblockerConfigManager.get().helpers.jerry.enableJerryTimer) return;
			String text = message.getString();
			//This part of hypixel still uses legacy text formatting, so we can't directly check for the actual text
			if (!text.startsWith("§b ☺ ") || !text.contains("Jerry")) return;
			HoverEvent hoverEvent = message.getStyle().getHoverEvent();
			if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) return;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			Scheduler.INSTANCE.schedule(() -> {
				if (player == null || !Utils.isOnSkyblock()) return;
				player.sendMessage(Constants.PREFIX.get().append(Text.literal("Jerry cooldown is over!")).formatted(Formatting.GREEN), false);
				player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.NEUTRAL, 100f, 1.0f);
			}, 20*60*6); // 6 minutes
		});
	}
}
