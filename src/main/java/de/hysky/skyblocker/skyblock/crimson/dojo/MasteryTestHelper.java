package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class MasteryTestHelper {

    private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");
    private static final int BLOCK_LIFE_TIME = 6850;

    private static final List<BlockPos> blockOrder = new ArrayList<>();
    private static final Map<BlockPos, Long> endTimes = new HashMap<>();

    protected static void reset() {
        blockOrder.clear();
        endTimes.clear();
    }

    protected static void onBlockUpdate(BlockPos pos, BlockState state) {
        if (state.isOf(Blocks.LIME_WOOL)) {
            blockOrder.add(pos);
            endTimes.put(pos, System.currentTimeMillis() + BLOCK_LIFE_TIME);
        }
        if (state.isAir()) {
            blockOrder.remove(pos);
            endTimes.remove(pos);
        }
    }

    protected static void render(WorldRenderContext context) {
        //render connecting lines
        if (!blockOrder.isEmpty()) {
            RenderHelper.renderLineFromCursor(context, blockOrder.getFirst().toCenterPos(), Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1f, 2);
        }
        if (blockOrder.size() >= 2) {
            RenderHelper.renderLinesFromPoints(context, new Vec3d[]{blockOrder.get(0).toCenterPos(), blockOrder.get(1).toCenterPos()}, Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1, 2, false);
        }

        //render times
        long currentTime = System.currentTimeMillis();
        for (BlockPos pos : blockOrder) {
            long blockEndTime = endTimes.get(pos);
            float secondsTime = Math.max((blockEndTime - currentTime) / 1000f, 0);
            RenderHelper.renderText(context, Text.literal(FORMATTER.format(secondsTime)), pos.add(0, 1, 0).toCenterPos(), 3, true);
        }
    }
}
