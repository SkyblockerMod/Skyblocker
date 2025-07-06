package de.hysky.skyblocker.utils.render;

import org.joml.FrustumIntersection;

import de.hysky.skyblocker.mixins.accessors.FrustumInvoker;
import de.hysky.skyblocker.mixins.accessors.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;

public class FrustumUtils {
    public static Frustum getFrustum() {
        return ((WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer).getFrustum();
    }

    public static boolean isVisible(Box box) {
        return getFrustum().isVisible(box);
    }

    public static boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        int plane = ((FrustumInvoker) getFrustum()).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ);

        return plane == FrustumIntersection.INSIDE || plane == FrustumIntersection.INTERSECT;
    }
}
