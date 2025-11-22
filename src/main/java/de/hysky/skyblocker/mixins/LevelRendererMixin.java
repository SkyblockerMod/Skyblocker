package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.dwarven.BlockBreakPrediction;
import net.minecraft.client.render.state.BreakingBlockRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	@ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;appearsGlowing()Z"))
	private boolean skyblocker$markCustomGlowUsedThisFrame(boolean hasVanillaGlow, @Local EntityRenderState entityRenderState) {
		boolean hasCustomGlow = entityRenderState.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW;

		if (hasCustomGlow) {
			this.levelRenderState.setData(MobGlow.FRAME_USES_CUSTOM_GLOW, true);
		}

		return hasVanillaGlow || hasCustomGlow;
	}

	@Inject(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;shouldShowEntityOutlines()Z")),
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;ILcom/mojang/blaze3d/textures/GpuTexture;D)V", ordinal = 0, shift = At.Shift.AFTER)
	)
	private void skyblocker$updateGlowDepthTexDepth(CallbackInfo ci) {
		if (this.levelRenderState.getDataOrDefault(MobGlow.FRAME_USES_CUSTOM_GLOW, false)) {
			GlowRenderer.getInstance().updateGlowDepthTexDepth();
		}
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
	private void skyblocker$drawGlowVertexConsumers(CallbackInfo ci) {
		GlowRenderer.getInstance().getGlowVertexConsumers().endOutlineBatch();
	}

	@Redirect(method = "fillBlockBreakingProgressRenderState", at = @At(value = "NEW", target = "(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/client/render/state/BreakingBlockRenderState;"))
	private BreakingBlockRenderState skyblocker$addBlockBreakingProgressRenderState(ClientWorld world, BlockPos entityBlockPos, int breakProgress) {
		//todo check setting
		int pingModifiedProgress = BlockBreakPrediction.getBlockBreakPrediction(entityBlockPos, breakProgress);
		return new BreakingBlockRenderState(world, entityBlockPos, pingModifiedProgress);
	}
}
