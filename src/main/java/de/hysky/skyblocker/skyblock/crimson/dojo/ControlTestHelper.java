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
    private static long pingMultiplier;

    protected static void reset() {
        correctWitherSkeleton = null;
        pingMultiplier = 0;
    }

    protected static void onEntitySpawn(Entity entity) {
        if (entity instanceof WitherSkeletonEntity witherSkeleton && correctWitherSkeleton == null) {
            correctWitherSkeleton = witherSkeleton;
            pingMultiplier = Util.getMeasuringTimeMs() / 100000;
        }
    }

    protected static void render(WorldRenderContext context) {
        if (correctWitherSkeleton != null) {
            Vec3d movementVector = correctWitherSkeleton.getPos().subtract(new Vec3d(correctWitherSkeleton.prevX, correctWitherSkeleton.prevY, correctWitherSkeleton.prevZ));
            Vec3d offset = movementVector.multiply(pingMultiplier);
            Vec3d aimPos = correctWitherSkeleton.getEyePos().add(offset);
            RenderHelper.renderLineFromCursor(context, aimPos, Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1, 3);
        }
    }
}
