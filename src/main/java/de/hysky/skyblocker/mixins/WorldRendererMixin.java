package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@ModifyExpressionValue(method = {"getEntitiesToRender", "renderEntities"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"), require = 2)
	private boolean skyblocker$shouldMobGlow(boolean original, @Local Entity entity) {
		boolean allowGlow = LividColor.allowGlow();
		boolean customGlow = MobGlow.hasOrComputeMobGlow(entity);
		return allowGlow && original || customGlow;
	}

	@Inject(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;canDrawEntityOutlines()Z")),
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;ILcom/mojang/blaze3d/textures/GpuTexture;D)V", ordinal = 0, shift = At.Shift.AFTER)
	)
	private void skyblocker$updateGlowDepthTexDepth(CallbackInfo ci) {
		if (MobGlow.atLeastOneMobHasCustomGlow()) {
			GlowRenderer.getInstance().updateGlowDepthTexDepth();
		}
	}

	@ModifyExpressionValue(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilderStorage;getOutlineVertexConsumers()Lnet/minecraft/client/render/OutlineVertexConsumerProvider;"))
	private OutlineVertexConsumerProvider skyblocker$useCustomGlowOutlineVertexConsumers(OutlineVertexConsumerProvider original, @Local Entity entity) {
		return MobGlow.getMobGlowOrDefault(entity, MobGlow.NO_GLOW) != MobGlow.NO_GLOW && !entity.isGlowing() ? GlowRenderer.getInstance().getGlowVertexConsumers() : original;
	}

	@ModifyVariable(method = "renderEntities",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V")),
			at = @At("STORE"), ordinal = 0
	)
	private int skyblocker$modifyGlowColor(int color, @Local Entity entity) {
		return MobGlow.getMobGlowOrDefault(entity, color);
	}

	@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
	private void skyblocker$renderMobBoundingBox(CallbackInfo ci, @Local Entity entity) {
		boolean shouldShowBoundingBox = MobBoundingBoxes.shouldDrawMobBoundingBox(entity);

		if (shouldShowBoundingBox) {
			MobBoundingBoxes.submitBox2BeRendered(
					entity instanceof ArmorStandEntity e ? SlayerManager.getSlayerMobBoundingBox(e) : entity.getBoundingBox(),
					MobBoundingBoxes.getBoxColor(entity)
			);
		}
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V"))
	private void skyblocker$drawGlowVertexConsumers(CallbackInfo ci) {
		GlowRenderer.getInstance().getGlowVertexConsumers().draw();
	}
}
