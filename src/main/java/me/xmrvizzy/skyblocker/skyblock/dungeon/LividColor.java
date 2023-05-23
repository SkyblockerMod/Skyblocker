package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class LividColor {
    private static int tenTicks = 0;

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (SkyblockerConfig.get().locations.dungeons.lividColor && message.getString().equals("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) {
                tenTicks = 8;
            }
            return true;
        });
    }

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (tenTicks != 0) {
            if (SkyblockerConfig.get().locations.dungeons.lividColor && Utils.isInDungeons() && client.world != null) {
                if (tenTicks == 1) {
                    SkyblockerMod.getInstance().messageScheduler.sendMessageAfterCooldown(SkyblockerConfig.get().locations.dungeons.lividColorText.replace("[color]", "red"));
                    tenTicks = 0;
                    return;
                }
                String key = client.world.getBlockState(new BlockPos(5, 110, 42)).getBlock().getTranslationKey();
                if (key.startsWith("block.minecraft.") && key.endsWith("wool") && !key.endsWith("red_wool")) {
                    SkyblockerMod.getInstance().messageScheduler.sendMessageAfterCooldown(SkyblockerConfig.get().locations.dungeons.lividColorText.replace("[color]", key.substring(16, key.length() - 5)));
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
