package de.hysky.skyblocker.utils.scheduler;

import net.minecraft.client.MinecraftClient;

/**
 * A scheduler for sending chat messages or commands. Use the instance in {@link #INSTANCE}. Do not instantiate this class.
 */
public class MessageScheduler extends Scheduler {
    /**
     * The minimum delay that the server will accept between chat messages.
     */
    private static final int MIN_DELAY = 200;
    public static final MessageScheduler INSTANCE = new MessageScheduler();
    /**
     * The timestamp of the last message send,
     */
    private long lastMessage = 0;

    protected MessageScheduler() {
    }

    /**
     * Sends a chat message or command after the minimum cooldown. Prefer this method to send messages or commands to the server.
     *
     * @param message the message to send
     */
    public void sendMessageAfterCooldown(String message) {
        if (lastMessage + MIN_DELAY < System.currentTimeMillis()) {
            sendMessage(message);
            lastMessage = System.currentTimeMillis();
        } else {
            queueMessage(message, 0);
        }
    }

    private void sendMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            if (message.startsWith("/")) {
                MinecraftClient.getInstance().player.networkHandler.sendCommand(message.substring(1));
            } else {
                MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(message);
                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(message);
            }
        }
    }

    /**
     * Queues a chat message or command to send in {@code delay} ticks. Use this method to send messages or commands a set time in the future. The minimum cooldown is still respected.
     *
     * @param message the message to send
     * @param delay   the delay before sending the message in ticks
     */
    public void queueMessage(String message, int delay) {
        schedule(() -> sendMessage(message), delay);
    }

    @Override
    protected boolean runTask(Runnable task, boolean multithreaded) {
        if (lastMessage + MIN_DELAY < System.currentTimeMillis()) {
            task.run();
            lastMessage = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
