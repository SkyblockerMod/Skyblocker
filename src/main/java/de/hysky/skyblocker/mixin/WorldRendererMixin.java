package de.hysky.skyblocker.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "render", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntities()Ljava/lang/Iterable;"), to = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;age:I", opcode = Opcodes.GETFIELD)), at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;regularEntityCount:I", opcode = Opcodes.PUTFIELD))
	private void skyblocker$beforeEntityIsRendered(CallbackInfo ci, @Local Entity entity) {
		boolean shouldShowBoundingBox = MobBoundingBoxes.shouldDrawMobBoundingBox(entity);

		if (shouldShowBoundingBox) {
			MobBoundingBoxes.submitBox2BeRendered(entity.getBoundingBox(), MobBoundingBoxes.getBoxColor(entity));
		}
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
	private boolean skyblocker$shouldMobGlow(boolean original, @Local Entity entity, @Share("hasCustomGlow") LocalBooleanRef hasCustomGlow) {
		boolean shouldGlow = MobGlow.shouldMobGlow(entity);
		hasCustomGlow.set(shouldGlow);
		return original || shouldGlow;
	}

	@ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
	private int skyblocker$modifyGlowColor(int color, @Local Entity entity, @Share("hasCustomGlow") LocalBooleanRef hasCustomGlow) {
		return hasCustomGlow.get() ? MobGlow.getGlowColor(entity) : color;
	}
}
