package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkyblockXpMessages {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern SKYBLOCK_XP_PATTERN = Pattern.compile("\\+\\d+ SkyBlock XP \\([^()]+\\) \\(\\d+/\\d+\\)");
	private static final IntOpenHashSet RECENT_MESSAGES = new IntOpenHashSet();

	@Init
	public static void init() {
		ChatEvents.RECEIVE_OVERLAY_STRING.register(SkyblockXpMessages::onOverlayMessage);
	}

	private static void onOverlayMessage(String message) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().chat.skyblockXpMessages) {
			Matcher matcher = SKYBLOCK_XP_PATTERN.matcher(message);

			if (matcher.find()) {
				String xpMessage = matcher.group();
				int hash = xpMessage.hashCode();

				if (!RECENT_MESSAGES.contains(hash)) {
					CLIENT.player.sendMessage(Constants.PREFIX.get().append(xpMessage), false);
					RECENT_MESSAGES.add(hash);
					Scheduler.INSTANCE.schedule(() -> RECENT_MESSAGES.remove(hash), 20 * 10);
				}
			}
		}
	}
}
