package de.hysky.skyblocker.mixins;


import de.hysky.skyblocker.skyblock.garden.GreenhousePaste;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
	@Inject(method = "setBlockState", at = @At("RETURN"))
	private void onSetBlockState(
			BlockPos pos,
			BlockState newState,
			int flags,
			CallbackInfoReturnable<BlockState> cir
	) {
		GreenhousePaste.onBlockChange(pos);
	}
}
