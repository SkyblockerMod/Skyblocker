package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(OutlineVertexConsumerProvider.class)
public interface OutlineVertexConsumerProviderAccessor {

	@Accessor
	@Mutable
	void setPlainDrawer(VertexConsumerProvider.Immediate immediate);
}
