package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

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
    public static final DungeonsConfig.Livid CONFIG = SkyblockerConfigManager.get().dungeons.livid;
    private static Formatting color = Formatting.AQUA;
    private static Block lastColor = Blocks.AIR;

    private static boolean isInitialized = false;
    /**
     * The correct livid may change color in M5, so we use the entity id to track the correct original livid.
     */
    private static boolean correctLividIdFound = false;
    private static int correctLividId = 0;
    private static final long OFFSET_DURATION = 2000;
    private static long toggleTime = 0;

    @Init
    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> LividColor.reset());
        WorldRenderEvents.AFTER_ENTITIES.register(LividColor::update);
    }

    private static void update(WorldRenderContext context) {
        DungeonsConfig.Livid config = SkyblockerConfigManager.get().dungeons.livid;
        if (!(config.enableLividColorText || config.enableLividColorTitle || config.enableLividColorGlow || config.enableLividColorBoundingBox)) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (!(Utils.isInDungeons() && DungeonManager.isInBoss() && client.player != null && client.world != null)) return;

        Block currentColor = client.world.getBlockState(new BlockPos(5, 110, 42)).getBlock();
        if (!(WOOL_TO_FORMATTING.containsKey(currentColor) && !currentColor.equals(lastColor))) return;

        if (!isInitialized && client.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            toggleTime = System.currentTimeMillis();
            isInitialized = true;
        } else if (isInitialized && System.currentTimeMillis() - toggleTime >= OFFSET_DURATION) {
            onLividColorFound(client, currentColor);
            if (!correctLividIdFound) {
                String lividName = LIVID_TO_FORMATTING.entrySet().stream()
                        .filter(entry -> entry.getValue() == color)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("unknown");
                client.world.getPlayers().stream()
                        .filter(entity -> entity.getName().getString().equals(lividName))
                        .findFirst()
                        .ifPresent(entity -> correctLividId = entity.getId());
                correctLividIdFound = true;
            }
            lastColor = currentColor;
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
    }

    public static boolean allowGlow() {
        return !SkyblockerConfigManager.get().dungeons.livid.enableLividColorGlow || !DungeonManager.getBoss().isFloor(5);
    }

    public static boolean shouldGlow(String name) {
        return SkyblockerConfigManager.get().dungeons.livid.enableLividColorGlow && color == LIVID_TO_FORMATTING.get(name);
    }

    public static boolean shouldDrawBoundingBox(String name) {
        return SkyblockerConfigManager.get().dungeons.livid.enableLividColorBoundingBox && color == LIVID_TO_FORMATTING.get(name);
    }

    @SuppressWarnings("DataFlowIssue")
    public static int getGlowColor(String name) {
        if (SkyblockerConfigManager.get().dungeons.livid.enableSolidColor) return Formatting.RED.getColorValue();
        if (LIVID_TO_FORMATTING.containsKey(name)) return LIVID_TO_FORMATTING.get(name).getColorValue();
        return Formatting.WHITE.getColorValue();
    }

    public static int getCorrectLividId() {
        return correctLividId;
    }

    private static void reset() {
        lastColor = Blocks.AIR;
        toggleTime = 0;
        isInitialized = false;
        correctLividIdFound = false;
        correctLividId = 0;
        color = Formatting.AQUA;
    }
}
