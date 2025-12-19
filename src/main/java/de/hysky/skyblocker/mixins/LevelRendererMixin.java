package de.hysky.skyblocker.mixins;

import org.jspecify.annotations.Nullable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.BlockBreakPrediction;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.injected.EntityRenderMarker;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements EntityRenderMarker {
	@Shadow
	@Final
	private LevelRenderState levelRenderState;
	@Unique
	private @Nullable EntityRenderState currentEntityStateBeingRendered;

	@Override
	public @Nullable EntityRenderState skyblocker$getEntityStateBeingRendered() {
		return this.currentEntityStateBeingRendered;
	}

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

	@Inject(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
	private void skyblocker$markEntityStateBeingRendered(CallbackInfo ci, @Local EntityRenderState state) {
		this.currentEntityStateBeingRendered = state;
	}

	@Inject(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", shift = At.Shift.AFTER))
	private void skyblocker$clearEntityStateBeingRendered(CallbackInfo ci) {
		this.currentEntityStateBeingRendered = null;
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
	private void skyblocker$drawGlowVertexConsumers(CallbackInfo ci) {
		GlowRenderer.getInstance().getGlowVertexConsumers().endOutlineBatch();
	}

	@Redirect(method = "extractBlockDestroyAnimation", at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/client/renderer/state/BlockBreakingRenderState;"))
	private BlockBreakingRenderState skyblocker$addBlockBreakingProgressRenderState(ClientLevel clientLevel, BlockPos blockPos, int i) {
		if (SkyblockerConfigManager.get().mining.BlockBreakPrediction.enabled) {
			int pingModifiedProgress = BlockBreakPrediction.getBlockBreakPrediction(blockPos, i);
			return new BlockBreakingRenderState(clientLevel, blockPos, pingModifiedProgress);

		}
		//if the setting is enabled do not modify anything
		else {
			return new BlockBreakingRenderState(clientLevel, blockPos, i);
		}

	}
}
