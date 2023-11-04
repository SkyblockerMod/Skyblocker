package de.hysky.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.skyblock.dungeon.MobGlow;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
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
