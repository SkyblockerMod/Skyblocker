package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class ControlTestHelper {

    private static WitherSkeletonEntity correctWitherSkeleton;
    private static long ping;
    private static Vec3d lastTargetPos;
    private static Vec3d lastPos;
    private static Vec3d aimOffset;

    protected static void reset() {
        correctWitherSkeleton = null;
        ping = 0;
        lastTargetPos = null;
        lastPos = null;
        aimOffset = null;
    }

    /**
     * Find the correct WitherSkeleton entity when it spawns to start tracking it
     *
     * @param entity spawned entity
     */
    protected static void onEntitySpawn(Entity entity) {
        if (entity instanceof WitherSkeletonEntity witherSkeleton && correctWitherSkeleton == null) {
            correctWitherSkeleton = witherSkeleton;
            ping = Util.getMeasuringTimeMs();
            aimOffset = new Vec3d(0, 0, 0);
        }
    }

    /**
     * update the aim offset for the wither skeleton based on ping
     */
    protected static void update() {
        if (correctWitherSkeleton != null) {
            //smoothly adjust the ping throughout the test
            ping = (ping + Util.getMeasuringTimeMs()) / 2;
            if (lastPos != null) {
                lastTargetPos = aimOffset;
                double ping = (double) ControlTestHelper.ping / 1000000;
                //find distance between last position and current position of skeleton
                Vec3d movementVector = correctWitherSkeleton.getPos().subtract(lastPos).multiply(1, 0.1, 1);
                //adjust the vector to current ping (20 / 3 is used because that is how many times this is updated a second. Every 3 ticks)
                movementVector = movementVector.multiply((double) 20 / 3 * ping);
                //smoothly adjust the aim offset based on the new value
                aimOffset = (aimOffset.add(movementVector)).multiply(0.5);
            }
            lastPos = correctWitherSkeleton.getPos();
        }
    }

    /**
     * Renders a line from the cursor where the player should aim
     *
     * @param context render context
     */
    protected static void render(WorldRenderContext context) {
        if (correctWitherSkeleton != null && aimOffset != null && lastTargetPos != null) {
            Vec3d aimPos = correctWitherSkeleton.getEyePos().add(aimOffset);
            RenderHelper.renderLineFromCursor(context, aimPos, Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1, 3);
        }
    }
}
