package me.xmrvizzy.skyblocker.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.Frustum;

/**
 * For the purpose of avoiding object allocations!
 *
 */
@Mixin(Frustum.class)
public interface FrustumInvoker {

	@Invoker("isVisible")
	public boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
