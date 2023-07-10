package me.xmrvizzy.skyblocker.utils;

import me.xmrvizzy.skyblocker.mixin.AccessorWorldRenderer;
import me.xmrvizzy.skyblocker.mixin.accessor.FrustumInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;

public class FrustumUtils {

    public static Frustum getFrustum() {
        return ((AccessorWorldRenderer) MinecraftClient.getInstance().worldRenderer).getFrustum();
    }

    public static boolean isBoxVisible(Box box) {
        return getFrustum().isVisible(box);
    }

    public static boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return ((FrustumInvoker) getFrustum()).isVisible(minX, minY, minZ, maxX, maxY, maxZ);
    }
}