package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class LividColor {
    private static final Map<Block, Formatting> WOOL_TO_FORMATTING = Map.of(
            Blocks.WHITE_WOOL, Formatting.WHITE,
            Blocks.MAGENTA_WOOL, Formatting.LIGHT_PURPLE,
            Blocks.RED_WOOL, Formatting.RED,
            Blocks.GRAY_WOOL, Formatting.GRAY,
            Blocks.GREEN_WOOL, Formatting.DARK_GREEN,
            Blocks.LIME_WOOL, Formatting.GREEN,
            Blocks.BLUE_WOOL, Formatting.BLUE,
            Blocks.PURPLE_WOOL, Formatting.DARK_PURPLE,
            Blocks.YELLOW_WOOL, Formatting.YELLOW
    );
    private static int tenTicks = 0;
    private static Formatting color;

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
                    onLividColorFound(Blocks.RED_WOOL);
                    return;
                }
                Block color = client.world.getBlockState(new BlockPos(5, 110, 42)).getBlock();
                if (WOOL_TO_FORMATTING.containsKey(color) && !color.equals(Blocks.RED_WOOL)) {
                    onLividColorFound(color);
                    return;
                }
                tenTicks--;
            } else {
                tenTicks = 0;
            }
        }
    }

    private static void onLividColorFound(Block color) {
        LividColor.color = WOOL_TO_FORMATTING.get(color);
        String colorString = Registries.BLOCK.getId(color).getPath();
        MessageScheduler.INSTANCE.sendMessageAfterCooldown(SkyblockerConfigManager.get().locations.dungeons.lividColor.lividColorText.replace("[color]", colorString.substring(0, colorString.length() - 5)));
        tenTicks = 0;
    }

    public static boolean shouldGlow(String name) {
        return switch (name) {
            case "Arcade Livid" -> color == Formatting.YELLOW;
            case "Crossed Livid" -> color == Formatting.LIGHT_PURPLE;
            case "Doctor Livid" -> color == Formatting.GRAY;
            case "Frog Livid" -> color == Formatting.DARK_GREEN;
            case "Hockey Livid" -> color == Formatting.RED;
            case "Purple Livid" -> color == Formatting.DARK_PURPLE;
            case "Scream Livid" -> color == Formatting.BLUE;
            case "Smile Livid" -> color == Formatting.GREEN;
            case "Vendetta Livid" -> color == Formatting.WHITE;

            default -> false;
        };
    }

    public static int getGlowColor(String name) {
        return switch (name) {
            case "Arcade Livid" -> Formatting.YELLOW.getColorValue();
            case "Crossed Livid" -> Formatting.LIGHT_PURPLE.getColorValue();
            case "Doctor Livid" -> Formatting.GRAY.getColorValue();
            case "Frog Livid" -> Formatting.DARK_GREEN.getColorValue();
            case "Hockey Livid" -> Formatting.RED.getColorValue();
            case "Purple Livid" -> Formatting.DARK_PURPLE.getColorValue();
            case "Scream Livid" -> Formatting.BLUE.getColorValue();
            case "Smile Livid" -> Formatting.GREEN.getColorValue();
            case "Vendetta Livid" -> Formatting.WHITE.getColorValue();

            default -> Formatting.WHITE.getColorValue();
        };
    }
}
