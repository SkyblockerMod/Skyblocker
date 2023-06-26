package me.xmrvizzy.skyblocker.mixin.accessor;

import me.xmrvizzy.skyblocker.utils.FrustumUtils;
import net.minecraft.client.render.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Use {@link FrustumUtils#isVisible(double, double, double, double, double, double) FrustumUtils#isVisible} which is shorter. For the purpose of avoiding object allocations!
 */
@Mixin(Frustum.class)
public interface FrustumInvoker {
    @Invoker("isVisible")
    boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
