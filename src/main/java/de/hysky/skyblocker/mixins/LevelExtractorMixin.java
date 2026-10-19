package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.BlockBreakPrediction;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

	@WrapOperation(method = "extractBlockDestroyAnimation", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/client/renderer/state/level/BlockBreakingRenderState;"))
	private BlockBreakingRenderState skyblocker$addBlockBreakingProgressRenderState(BlockPos pos, BlockState state, int progress, Operation<BlockBreakingRenderState> original) {
		if (SkyblockerConfigManager.get().mining.blockBreakPrediction.enabled) {
			int pingModifiedProgress = BlockBreakPrediction.getBlockBreakPrediction(pos, progress);
			return new BlockBreakingRenderState(pos, state, pingModifiedProgress);

		}
		//if the setting is not enabled do not modify anything
		else {
			return original.call(pos, state, progress);
		}
	}
}
