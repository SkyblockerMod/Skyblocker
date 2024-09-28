package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkyblockXpMessages {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern SKYBLOCK_XP_PATTERN = Pattern.compile("§b\\+\\d+ SkyBlock XP §7\\([^()]+§7\\)§b \\(\\d+\\/\\d+\\)");
	private static final IntOpenHashSet RECENT_MESSAGES = new IntOpenHashSet();

	@Init
	public static void init() {
		ClientReceiveMessageEvents.GAME.register(SkyblockXpMessages::onMessage);
	}

	private static void onMessage(Text text, boolean overlay) {
		if (Utils.isOnSkyblock() && overlay && SkyblockerConfigManager.get().chat.skyblockXpMessages) {
			String message = text.getString();
			Matcher matcher = SKYBLOCK_XP_PATTERN.matcher(message);

			if (matcher.find()) {
				String xpMessage = matcher.group();
				int hash = xpMessage.hashCode();

				if (!RECENT_MESSAGES.contains(hash)) {
					CLIENT.player.sendMessage(Constants.PREFIX.get().append(xpMessage));
					RECENT_MESSAGES.add(hash);
					Scheduler.INSTANCE.schedule(() -> RECENT_MESSAGES.remove(hash), 20 * 10);
				}
			}
		}
	}
}
