package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.InactivityFpsLimiterAccessor;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.Random;

public class BreakReminder {
	private static final long WARNING_INTERVAL = 60 * 60 * 1000;
	private static final long AFK_REQUIRED_FOR_BREAK = 90 * 1000;
	private static final int DESCRIPTION_COUNT = 6;

	private static long startedPlayingMillis;
	private static long lastWarningMillis;

	@Init
	public static void init() {
		SkyblockEvents.JOIN.register(() -> {
			long l = Util.getMeasuringTimeMs();
			startedPlayingMillis = l;
			lastWarningMillis = l;
		});
		Scheduler.INSTANCE.scheduleCyclic(BreakReminder::tick, 20 * 30, true);
	}

	private static void tick() {
		if (!Utils.isOnSkyblock() || SkyblockerConfigManager.get().misc.disableBreakReminders) return;
		long time = Util.getMeasuringTimeMs();
		MinecraftClient client = MinecraftClient.getInstance();
		if (time - ((InactivityFpsLimiterAccessor) client.getInactivityFpsLimiter()).getLastInputTime() > AFK_REQUIRED_FOR_BREAK) {
			lastWarningMillis = time;
		}
		if (time - lastWarningMillis > WARNING_INTERVAL) {
			lastWarningMillis = time;
			client.execute(BreakReminder::warnCouchPotato);
		}
	}

	private static void warnCouchPotato() {
		MinecraftClient client = MinecraftClient.getInstance();
		long playingFor = lastWarningMillis - startedPlayingMillis;
		int playingForHours = (int) (playingFor / 3600_000);
		client.getToastManager().add(SystemToast.create(
				client,
				SystemToast.Type.PERIODIC_NOTIFICATION,
				Text.translatable("skyblocker.potato.title", playingForHours),
				Text.translatable("skyblocker.potato.description" + new Random().nextInt(DESCRIPTION_COUNT))
		));
	}
}
