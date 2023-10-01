package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
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
                    MessageScheduler.INSTANCE.sendMessageAfterCooldown(SkyblockerConfigManager.get().locations.dungeons.lividColor.lividColorText.replace("[color]", "red"));
                    tenTicks = 0;
                    return;
                }
                String key = client.world.getBlockState(new BlockPos(5, 110, 42)).getBlock().getTranslationKey();
                if (key.startsWith("block.minecraft.") && key.endsWith("wool") && !key.endsWith("red_wool")) {
                    MessageScheduler.INSTANCE.sendMessageAfterCooldown(SkyblockerConfigManager.get().locations.dungeons.lividColor.lividColorText.replace("[color]", key.substring(16, key.length() - 5)));
                    tenTicks = 0;
                    return;
                }
                tenTicks--;
            } else {
                tenTicks = 0;
            }
        }
    }
}
