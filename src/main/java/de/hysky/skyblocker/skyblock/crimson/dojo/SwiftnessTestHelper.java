package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SwiftnessTestHelper {

    private static BlockPos lastBlock;

    protected static void reset() {
        lastBlock = null;
    }

    public static void onBlockUpdate(BlockPos pos, BlockState state) {
        if (state.isOf(Blocks.LIME_WOOL)) {
            lastBlock = pos.toImmutable();
        }
    }

    protected static void render(WorldRenderContext context) {
        if (lastBlock == null) {
            return;
        }
        System.out.println("render" + lastBlock);
        RenderHelper.renderFilled(context,lastBlock, new float[]{0f, 1f, 0f}, 0.5f, true);
    }
}
