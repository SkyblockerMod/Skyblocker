package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.*;

public class MasteryTestHelper {

    private static final float[] LIGHT_GRAY = { 192 / 255f, 192 / 255f, 192 / 255f };
    private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");

    private static List<BlockPos> blockOrder = new ArrayList<>();
    private static Map<BlockPos, Long> endTimes =new HashMap<>();

    protected static void reset() {
        blockOrder = new ArrayList<>();
        endTimes =new HashMap<>();
    }


    public static void onBlockUpdate(BlockUpdateS2CPacket packet) {
        BlockPos pos = packet.getPos();
        if (packet.getState().isOf(Blocks.LIME_WOOL)) {
            blockOrder.add(pos);
            endTimes.put(pos,System.currentTimeMillis()+6850);
        }
        if (packet.getState().isAir()) {
            blockOrder.remove(pos);
        }
    }

    protected static void render(WorldRenderContext context) {
        //render connecting lines
        if (!blockOrder.isEmpty()) {
            RenderHelper.renderLineFromCursor(context, blockOrder.getFirst().toCenterPos(),LIGHT_GRAY,1f, 2);
        }
        if (blockOrder.size() >= 2) {
            RenderHelper.renderLinesFromPoints(context, new Vec3d[]{blockOrder.get(0).toCenterPos(), blockOrder.get(1).toCenterPos()}, new float[]{0f, 1f, 0f}, 1, 2, false);
        }

        //render times
        long currentTime = System.currentTimeMillis();
        for (BlockPos pos : blockOrder) {
            long blockEndTime = endTimes.get(pos);
            float secondsTime  = Math.max((blockEndTime - currentTime) / 1000f, 0);
            RenderHelper.renderText(context, Text.literal(FORMATTER.format(secondsTime)), pos.add(0, 1, 0).toCenterPos(), 3, true);

        }

    }
}
