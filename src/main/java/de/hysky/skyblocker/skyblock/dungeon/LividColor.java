package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Set;

public class LividColor {
    private static final Map<Block, Formatting> WOOL_TO_FORMATTING = Map.of(
            Blocks.RED_WOOL, Formatting.RED,
            Blocks.YELLOW_WOOL, Formatting.YELLOW,
            Blocks.LIME_WOOL, Formatting.GREEN,
            Blocks.GREEN_WOOL, Formatting.DARK_GREEN,
            Blocks.BLUE_WOOL, Formatting.BLUE,
            Blocks.MAGENTA_WOOL, Formatting.LIGHT_PURPLE,
            Blocks.PURPLE_WOOL, Formatting.DARK_PURPLE,
            Blocks.GRAY_WOOL, Formatting.GRAY,
            Blocks.WHITE_WOOL, Formatting.WHITE
    );
    private static final Map<String, Formatting> LIVID_TO_FORMATTING = Map.of(
            "Hockey Livid", Formatting.RED,
            "Arcade Livid", Formatting.YELLOW,
            "Smile Livid", Formatting.GREEN,
            "Frog Livid", Formatting.DARK_GREEN,
            "Scream Livid", Formatting.BLUE,
            "Crossed Livid", Formatting.LIGHT_PURPLE,
            "Purple Livid", Formatting.DARK_PURPLE,
            "Doctor Livid", Formatting.GRAY,
            "Vendetta Livid", Formatting.WHITE
    );
    public static final Set<String> LIVID_NAMES = Set.copyOf(LIVID_TO_FORMATTING.keySet());
    public static final SkyblockerConfig.LividColor CONFIG = SkyblockerConfigManager.get().locations.dungeons.lividColor;
    private static int tenTicks = 0;
    private static Formatting color;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            SkyblockerConfig.LividColor config = SkyblockerConfigManager.get().locations.dungeons.lividColor;
            if ((config.enableLividColorText || config.enableLividColorTitle || config.enableLividColorGlow) && message.getString().equals("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) {
                tenTicks = 8;
            }
        });
    }

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (tenTicks != 0) {
            SkyblockerConfig.LividColor config = SkyblockerConfigManager.get().locations.dungeons.lividColor;
            if ((config.enableLividColorText || config.enableLividColorTitle || config.enableLividColorGlow) && Utils.isInDungeons() && client.world != null) {
                if (tenTicks == 1) {
                    onLividColorFound(client, Blocks.RED_WOOL);
                    return;
                }
                Block color = client.world.getBlockState(new BlockPos(5, 110, 42)).getBlock();
                if (WOOL_TO_FORMATTING.containsKey(color) && !color.equals(Blocks.RED_WOOL)) {
                    onLividColorFound(client, color);
                    return;
                }
                tenTicks--;
            } else {
                tenTicks = 0;
            }
        }
    }

    private static void onLividColorFound(MinecraftClient client, Block color) {
        LividColor.color = WOOL_TO_FORMATTING.get(color);
        String colorString = Registries.BLOCK.getId(color).getPath();
        colorString = colorString.substring(0, colorString.length() - 5).toUpperCase();
        MutableText message = Constants.PREFIX.get()
                .append(CONFIG.lividColorText.replaceAll("\\[color]", colorString))
                .formatted(LividColor.color);
        if (CONFIG.enableLividColorText) {
            MessageScheduler.INSTANCE.sendMessageAfterCooldown(message.getString());
        }
        if (CONFIG.enableLividColorTitle) {
            client.inGameHud.setDefaultTitleFade();
            client.inGameHud.setTitle(message);
        }
        tenTicks = 0;
    }

    public static boolean allowGlow() {
        return !SkyblockerConfigManager.get().locations.dungeons.lividColor.enableLividColorGlow || !DungeonManager.getBoss().isFloor(5);
    }

    public static boolean shouldGlow(String name) {
        return SkyblockerConfigManager.get().locations.dungeons.lividColor.enableLividColorGlow && color == LIVID_TO_FORMATTING.get(name);
    }

    @SuppressWarnings("DataFlowIssue")
    public static int getGlowColor(String name) {
        return LIVID_TO_FORMATTING.containsKey(name) ? LIVID_TO_FORMATTING.get(name).getColorValue() : Formatting.WHITE.getColorValue();
    }
}
