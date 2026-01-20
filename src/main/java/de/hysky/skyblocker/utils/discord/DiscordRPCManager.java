package de.hysky.skyblocker.utils.discord;


import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.MiscConfig;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Utils;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the discord rich presence. Automatically connects to discord and displays a customizable activity when playing Skyblock.
 */
public class DiscordRPCManager {
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.##");
	public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Discord RPC");
	/**
	 * The update task used to avoid multiple update tasks running simultaneously.
	 */
	private static final AtomicReference<CompletableFuture<Void>> updateTask = new AtomicReference<>();
	private static final AtomicLong startTimeStamp = new AtomicLong();
	private static final AtomicInteger cycleCount = new AtomicInteger();

	@Init
	public static void init() {
		SkyblockEvents.LEAVE.register(DiscordRPCManager::initAndUpdatePresence);
		SkyblockEvents.JOIN.register(() -> {
			startTimeStamp.set(System.currentTimeMillis());
			initAndUpdatePresence(true);
		});
	}

	/**
	 * Checks the {@link MiscConfig.RichPresence#customMessage custom message}, updates {@link #cycleCount} if enabled, and updates rich presence.
	 */
	public static void updateDataAndPresence() {
		// If the custom message is empty, discord will keep the last message, this is can serve as a default if the user doesn't want a custom message
		if (SkyblockerConfigManager.get().misc.richPresence.customMessage.isEmpty()) {
			SkyblockerConfigManager.update(config -> config.misc.richPresence.customMessage = "Playing Skyblock");
		}
		if (SkyblockerConfigManager.get().misc.richPresence.cycleMode) cycleCount.updateAndGet(count -> (count + 1) % 3);
		initAndUpdatePresence();
	}

	/**
	 * @see #initAndUpdatePresence(boolean)
	 */
	private static void initAndUpdatePresence() {
		initAndUpdatePresence(false);
	}

	/**
	 * Updates discord presence asynchronously.
	 * <p>
	 * When the {@link #updateTask previous update} does not exist or {@link CompletableFuture#isDone() has completed}:
	 * <p>
	 * Connects to discord if {@link MiscConfig.RichPresence#enableRichPresence rich presence is enabled},
	 * the player {@link Utils#isOnSkyblock() is on Skyblock}, and {@link DiscordIPC#isConnected() discord is not already connected}.
	 * Updates the presence if {@link MiscConfig.RichPresence#enableRichPresence rich presence is enabled}
	 * and the player {@link Utils#isOnSkyblock() is on Skyblock}.
	 * Stops the connection if {@link MiscConfig.RichPresence#enableRichPresence rich presence is disabled}
	 * or the player {@link Utils#isOnSkyblock() is not on Skyblock} and {@link DiscordIPC#isConnected() discord is connected}.
	 * Saves the update task in {@link #updateTask}
	 *
	 * @param initialization whether this is the first time the presence is being updates. If {@code true}, a message will be logged
	 *                       if {@link MiscConfig.RichPresence#enableRichPresence rich presence is disabled}.
	 */
	private static void initAndUpdatePresence(boolean initialization) {
		CompletableFuture<Void> currentTask = updateTask.get();
		if (currentTask == null || currentTask.isDone()) {
			CompletableFuture<Void> newTask = CompletableFuture.runAsync(() -> {
				if (SkyblockerConfigManager.get().misc.richPresence.enableRichPresence && Utils.isOnSkyblock()) {
					if (!DiscordIPC.isConnected()) {
						if (DiscordIPC.start(934607927837356052L, null)) {
							LOGGER.info("[Skyblocker] Discord RPC connected successfully");
						} else {
							if (initialization) {
								LOGGER.error("[Skyblocker] Discord RPC failed to connect");
							}
							return;
						}
					}
					DiscordIPC.setActivity(buildPresence());
				} else if (DiscordIPC.isConnected()) {
					DiscordIPC.stop();
					LOGGER.info("[Skyblocker] Discord RPC disconnected");
				} else if (initialization) {
					LOGGER.info("[Skyblocker] Discord RPC is currently disabled, will not connect");
				}
			}, Executors.newVirtualThreadPerTaskExecutor());
			updateTask.set(newTask);
		}
	}

	public static RichPresence buildPresence() {
		RichPresence presence = new RichPresence();
		presence.setLargeImage("skyblocker-default", null);
		presence.setStart(startTimeStamp.get());
		presence.setDetails(SkyblockerConfigManager.get().misc.richPresence.customMessage);
		presence.setState(getInfo());
		return presence;
	}

	public static String getInfo() {
		String info = null;
		if (!SkyblockerConfigManager.get().misc.richPresence.cycleMode) {
			switch (SkyblockerConfigManager.get().misc.richPresence.info) {
				case BITS -> info = "Bits: " + DECIMAL_FORMAT.format(Utils.getBits());
				case PURSE -> info = "Purse: " + DECIMAL_FORMAT.format(Utils.getPurse());
				case LOCATION -> info = Utils.getIslandArea();
			}
		} else if (SkyblockerConfigManager.get().misc.richPresence.cycleMode) {
			switch (cycleCount.get()) {
				case 0 -> info = "Bits: " + DECIMAL_FORMAT.format(Utils.getBits());
				case 1 -> info = "Purse: " + DECIMAL_FORMAT.format(Utils.getPurse());
				case 2 -> info = Utils.getIslandArea();
			}
		}
		return info;
	}
}
