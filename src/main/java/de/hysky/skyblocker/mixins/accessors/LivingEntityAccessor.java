package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor
	LivingEntity.SwingState getSwingState();

	@Invoker
	int invokeGetModifiedSwingDuration(SwingAnimation animation);
}
