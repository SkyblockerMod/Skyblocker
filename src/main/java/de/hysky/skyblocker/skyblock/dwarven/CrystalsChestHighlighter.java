package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class CrystalsChestHighlighter {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final String CHEST_SPAWN_MESSAGE = "You uncovered a treasure chest!";

    private static int waitingForChest = 0;
    private static final List<BlockPos> activeChests = new ArrayList<>();

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(CrystalsChestHighlighter::extractLocationFromMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CrystalsChestHighlighter::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
    }

    private static void reset() {
        waitingForChest = 0;
        activeChests.clear();
    }

    private static void extractLocationFromMessage(Text text, boolean b) {
        if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
            return;
        }
        //if a chest is spawned add chest to look for
        if (text.getString().matches(CHEST_SPAWN_MESSAGE)) {
            waitingForChest += 1;
        }
    }

    /**
     * When a block is updated in the crystal hollows if looking for a chest see if it's a chest and if so add to active. or remove active chests from where air is placed
     *
     * @param pos   location of block update
     * @param state the new state of the block
     */
    public static void onBlockUpdate(BlockPos pos, BlockState state) {
        if (!SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter || CLIENT.player == null) {
            return;
        }
        if (waitingForChest > 0 && state.isOf(Blocks.CHEST)) {
            //make sure it is not too far from the player (more than 10 blocks away)
            if (pos.getSquaredDistance(CLIENT.player.getPos()) > 100) {
                return;
            }
            activeChests.add(pos);
            waitingForChest -= 1;
        } else if (state.isAir()) {
            activeChests.remove(pos);
        }


    }

    /**
     * Renders a box around active treasure chests if enabled. Taking the color from the config
     *
     * @param context context
     */
    private static void render(WorldRenderContext context) {
        if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
            return;
        }
        float[] color = SkyblockerConfigManager.get().mining.crystalHollows.chestHighlightColor.getComponents(new float[]{0, 0, 0, 0});
        for (BlockPos chest : activeChests) {
            RenderHelper.renderOutline(context, Box.of(chest.toCenterPos().subtract(0, 0.0625, 0), 0.875, 0.875, 0.875), color, color[3], 3, true);
        }

    }
}
