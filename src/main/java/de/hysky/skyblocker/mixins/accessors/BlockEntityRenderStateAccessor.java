package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockEntityRenderState.class)
public interface BlockEntityRenderStateAccessor {

	@Accessor
	void setBlockState(BlockState blockState);
}
