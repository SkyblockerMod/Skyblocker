package me.xmrvizzy.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.dungeon.StarredMobGlow;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
	private boolean skyblocker$shouldStarredMobGlow(boolean original, @Local Entity entity, @Share("isGlowingStarredMob") LocalBooleanRef isGlowingStarredMob) {
		boolean isAStarredMobThatShouldGlow = SkyblockerConfig.get().locations.dungeons.starredMobGlow && StarredMobGlow.shouldMobGlow(entity);

		isGlowingStarredMob.set(isAStarredMobThatShouldGlow);

		return original || isAStarredMobThatShouldGlow;
	}

	@ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
	private int skyblocker$modifyGlowColor(int color, @Local Entity entity, @Share("isGlowingStarredMob") LocalBooleanRef isGlowingStarredMob) {
		return isGlowingStarredMob.get() ? StarredMobGlow.getGlowColor(entity) : color;
	}
}
