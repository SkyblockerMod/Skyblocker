package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TreeFelledNotification {
	private static final String PETALFALL_MESSAGE = "PETALFALL! You felled the entire Tree!";
	private static final String WOODPECKER_MESSAGE = "WOODPECKER! You felled the entire Tree!";
	private static final String TIMBER_MESSAGE = "TIMBER! You felled the entire Tree!";
	private static final Title TITLE = new Title("skyblocker.foraging.treeFelled", ChatFormatting.AQUA);

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(TreeFelledNotification::onMessage);
	}

	private static boolean onMessage(Component message, boolean overlay) {
		if (Utils.isOnSkyblock() && Utils.isInForagingIsland() && SkyblockerConfigManager.get().foraging.enableTreeFelledNotification) {
			String stringified = message.getString();

			if (stringified.equals(PETALFALL_MESSAGE) || stringified.equals(WOODPECKER_MESSAGE) || stringified.equals(TIMBER_MESSAGE)) {
				// Show title for 3 seconds (same as mining ability chat rule preset)
				TitleContainer.addTitle(TITLE, 3 * 20);
			}
		}

		return true;
	}
}
