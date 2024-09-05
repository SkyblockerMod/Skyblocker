package de.hysky.skyblocker.skyblock.chat.chatcoords;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class ChatLocation {
    @Init
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("skyblocker").then(ClientCommandManager.literal("location").executes(context -> sharePlayerLocation()))
        ));
    }

    private static int sharePlayerLocation() {
        ClientPlayerEntity thePlayer = MinecraftClient.getInstance().player;
        MessageScheduler.INSTANCE.sendMessageAfterCooldown("x: " + (int) thePlayer.getX() + ", y: " + (int) thePlayer.getY() + ", z: " + (int) thePlayer.getZ() + " | " + Utils.getIslandArea(), true);
        return Command.SINGLE_SUCCESS;
    }

}
