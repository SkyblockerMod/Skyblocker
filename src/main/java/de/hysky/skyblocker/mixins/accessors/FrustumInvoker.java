package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.render.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Use {@link de.hysky.skyblocker.utils.render.FrustumUtils#isVisible(double, double, double, double, double, double) FrustumUtils#isVisible} which is shorter. For the purpose of avoiding object allocations!
 */
@Mixin(Frustum.class)
public interface FrustumInvoker {
    @Invoker
    boolean invokeIsVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
