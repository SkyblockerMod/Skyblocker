package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.BlockBreakPrediction;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

	@Inject(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private void skyblocker$markCustomGlowUsedThisFrame(CallbackInfo ci, @Local(name = "output") LevelRenderState output, @Local(name = "state") EntityRenderState state) {
		if (state.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			output.setData(MobGlow.FRAME_USES_CUSTOM_GLOW, true);
		}
	}

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
