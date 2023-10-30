package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

public class LividColor {
    private static int tenTicks = 0;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (SkyblockerConfigManager.get().locations.dungeons.lividColor.enableLividColor && message.getString().equals("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) {
                tenTicks = 8;
            }
        });
    }

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (tenTicks != 0) {
            if (SkyblockerConfigManager.get().locations.dungeons.lividColor.enableLividColor && Utils.isInDungeons() && client.world != null) {
                if (tenTicks == 1) {
                    onLividColorFound("red");
                    return;
                }
                String key = Registries.BLOCK.getId(client.world.getBlockState(new BlockPos(5, 110, 42)).getBlock()).getPath();
                if (key.endsWith("wool") && !key.equals("red_wool")) {
                    onLividColorFound(key.substring(0, key.length() - 5));
                    return;
                }
                tenTicks--;
            } else {
                tenTicks = 0;
            }
        }
    }

    private static void onLividColorFound(String color) {
        MessageScheduler.INSTANCE.sendMessageAfterCooldown(SkyblockerConfigManager.get().locations.dungeons.lividColor.lividColorText.replace("[color]", color));
        tenTicks = 0;
    }
}
