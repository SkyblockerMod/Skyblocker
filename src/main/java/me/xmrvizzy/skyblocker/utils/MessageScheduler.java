package me.xmrvizzy.skyblocker.utils;

import net.minecraft.client.MinecraftClient;

/**
 * A scheduler for sending chat messages or commands. Use the instance in {@link me.xmrvizzy.skyblocker.SkyblockerMod#messageScheduler SkyblockerMod.messageScheduler}. Do not instantiate this class.
 */
@SuppressWarnings("deprecation")
public class MessageScheduler extends Scheduler {
    private long lastMessage = 0;

    public void sendMessageAfterCooldown(String message) {
        if (lastMessage + 200 < System.currentTimeMillis()) {
            sendMessage(message);
            lastMessage = System.currentTimeMillis();
        } else {
            tasks.add(new ScheduledTask(() -> sendMessage(message), 0));
        }
    }

    private void sendMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(message);
            if (message.startsWith("/")) {
                MinecraftClient.getInstance().player.networkHandler.sendCommand(message.substring(1));
            } else {
                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(message);
            }
        }
    }

    public void queueMessage(String message, int delay) {
        tasks.add(new ScheduledTask(() -> sendMessage(message), delay));
    }

    @Override
    protected void runTask(Runnable task) {
        if (lastMessage + 200 < System.currentTimeMillis()) {
            task.run();
            lastMessage = System.currentTimeMillis();
        }
    }
}
