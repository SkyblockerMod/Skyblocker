package de.hysky.skyblocker.mixins;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.injected.EntityRenderMarker;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

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

	@Inject(method = "lambda$addMainPass$0",
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;shouldShowEntityOutlines:Z", opcode = Opcodes.GETFIELD)),
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;D)V", ordinal = 0, shift = At.Shift.AFTER)
	)
	private void skyblocker$updateGlowDepthTexDepth(CallbackInfo ci) {
		if (this.levelRenderState.getDataOrDefault(MobGlow.FRAME_USES_CUSTOM_GLOW, false)) {
			GlowRenderer.INSTANCE.updateGlowDepthTexDepth();
		}
	}

	@Inject(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
	private void skyblocker$markEntityStateBeingRendered(CallbackInfo ci, @Local(name = "state") EntityRenderState state) {
		this.currentEntityStateBeingRendered = state;
	}

	@Inject(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", shift = At.Shift.AFTER))
	private void skyblocker$clearEntityStateBeingRendered(CallbackInfo ci) {
		this.currentEntityStateBeingRendered = null;
	}
}
