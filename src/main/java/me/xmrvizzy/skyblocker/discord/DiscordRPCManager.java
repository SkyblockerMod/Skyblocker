package me.xmrvizzy.skyblocker.discord;


import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.SkyblockEvents;
import me.xmrvizzy.skyblocker.utils.Utils;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;

public class DiscordRPCManager {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.##");
    public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Discord RPC");
    public static CompletableFuture<Void> updateTask;
    public static long startTimeStamp;
    public static int cycleCount;

    public static void init() {
        SkyblockEvents.LEAVE.register(DiscordRPCManager::initAndUpdatePresence);
        SkyblockEvents.JOIN.register(() -> {
            startTimeStamp = System.currentTimeMillis();
            initAndUpdatePresence(true);
        });
    }

    public static void updateDataAndPresence() {
        // If the custom message is empty, discord will keep the last message, this is can serve as a default if the user doesn't want a custom message
        if (SkyblockerConfig.get().richPresence.customMessage.isEmpty()) {
            SkyblockerConfig.get().richPresence.customMessage = "Playing Skyblock";
            AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
        }
        if (SkyblockerConfig.get().richPresence.cycleMode) cycleCount = (cycleCount + 1) % 3;
        initAndUpdatePresence();
    }

    private static void initAndUpdatePresence() {
        initAndUpdatePresence(false);
    }

    /**
     * Updates discord presence asynchronously.
     * <p>
     * When the {@link #updateTask previous update} does not exist or {@link CompletableFuture#isDone() has completed}:
     * <p>
     * Connects to discord if {@link SkyblockerConfig.RichPresence#enableRichPresence rich presence is enabled},
     * the player {@link Utils#isOnSkyblock() is on Skyblock}, and {@link DiscordIPC#isConnected() discord is not already connected}.
     * Updates the presence if {@link SkyblockerConfig.RichPresence#enableRichPresence rich presence is enabled}
     * and the player {@link Utils#isOnSkyblock() is on Skyblock}.
     * Stops the connection if {@link SkyblockerConfig.RichPresence#enableRichPresence rich presence is disabled}
     * or the player {@link Utils#isOnSkyblock() is not on Skyblock} and {@link DiscordIPC#isConnected() discord is connected}.
     * Saves the update task in {@link #updateTask}
     *
     * @param initialization whether this is the first time the presence is being updates. If {@code true}, a message will be logged
     *                       if {@link SkyblockerConfig.RichPresence#enableRichPresence rich presence is disabled}.
     */
    private static void initAndUpdatePresence(boolean initialization) {
        if (updateTask == null || updateTask.isDone()) {
            updateTask = CompletableFuture.runAsync(() -> {
                if (SkyblockerConfig.get().richPresence.enableRichPresence && Utils.isOnSkyblock()) {
                    if (!DiscordIPC.isConnected()) {
                        if (DiscordIPC.start(934607927837356052L, null)) {
                            LOGGER.info("Discord RPC started successfully");
                        } else {
                            LOGGER.error("Discord RPC failed to start");
                            return;
                        }
                    }
                    DiscordIPC.setActivity(buildPresence());
                } else if (DiscordIPC.isConnected()) {
                    DiscordIPC.stop();
                    LOGGER.info("Discord RPC stopped");
                } else if (initialization) {
                    LOGGER.info("Discord RPC is currently disabled");
                }
            });
        }
    }

    public static RichPresence buildPresence() {
        RichPresence presence = new RichPresence();
        presence.setLargeImage("skyblocker-default", null);
        presence.setStart(startTimeStamp);
        presence.setDetails(SkyblockerConfig.get().richPresence.customMessage);
        presence.setState(getInfo());
        return presence;
    }

    public static String getInfo() {
        String info = null;
        if (!SkyblockerConfig.get().richPresence.cycleMode) {
            switch (SkyblockerConfig.get().richPresence.info) {
                case BITS -> info = "Bits: " + DECIMAL_FORMAT.format(Utils.getBits());
                case PURSE -> info = "Purse: " + DECIMAL_FORMAT.format(Utils.getPurse());
                case LOCATION -> info = "⏣ " + Utils.getLocation();
            }
        } else if (SkyblockerConfig.get().richPresence.cycleMode) {
            switch (cycleCount) {
                case 0 -> info = "Bits: " + DECIMAL_FORMAT.format(Utils.getBits());
                case 1 -> info = "Purse: " + DECIMAL_FORMAT.format(Utils.getPurse());
                case 2 -> info = "⏣ " + Utils.getLocation();
            }
        }
        return info;
    }
}
