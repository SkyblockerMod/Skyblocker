package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.slayers.features.SlayerEntitiesGlow;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@ModifyExpressionValue(method = {"getEntitiesToRender", "renderEntities"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
	private boolean skyblocker$shouldMobGlow(boolean original, @Local Entity entity, @Share("hasCustomGlow") LocalBooleanRef hasCustomGlow) {
		return shouldMobGlow(original, entity, hasCustomGlow);
	}

	@WrapOperation(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getTeamColorValue()I"))
	private int skyblocker$modifyGlowColor(Entity instance, Operation<Integer> original, @Local Entity entity, @Share("hasCustomGlow") LocalBooleanRef hasCustomGlow) {
		return hasCustomGlow.get() ? MobGlow.getGlowColor(entity) : original.call(instance);
	}

	@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
	private void skyblocker$beforeEntityIsRendered(CallbackInfo ci, @Local Entity entity) {
		boolean shouldShowBoundingBox = MobBoundingBoxes.shouldDrawMobBoundingBox(entity);

		if (shouldShowBoundingBox) {
			MobBoundingBoxes.submitBox2BeRendered(
					entity instanceof ArmorStandEntity e ? SlayerEntitiesGlow.getSlayerMobBoundingBox(e) : entity.getBoundingBox(),
					MobBoundingBoxes.getBoxColor(entity)
			);
		}
	}

	@Unique
	private static boolean shouldMobGlow(boolean original, Entity entity, LocalBooleanRef hasCustomGlow) {
		boolean allowGlow = LividColor.allowGlow();
		boolean shouldGlow = MobGlow.shouldMobGlow(entity);
		hasCustomGlow.set(shouldGlow);
		return allowGlow && original || shouldGlow;
	}
}
