package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.voidgloom.LazerTimer;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	@Final
	private EntityType<?> type;

	@Shadow
	public abstract UUID getUuid();

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	public abstract BlockPos getBlockPos();

	@Shadow
	public abstract Vec3d getPos();

	@Shadow
	public abstract @Nullable Entity getVehicle();

	@Shadow
	public abstract boolean isInvisible();

	@ModifyExpressionValue(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
	public boolean skyblocker$showInvisibleArmorStands(boolean isSpectator, PlayerEntity player) {
		return isSpectator || (isInvisible() && Utils.isOnHypixel() && Debug.debugEnabled() && SkyblockerConfigManager.get().debug.showInvisibleArmorStands && type.equals(EntityType.ARMOR_STAND));
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("RETURN"))
	private void onStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			if (SkyblockerConfigManager.get().slayers.endermanSlayer.lazerTimer && SlayerManager.isBossSpawned() && this.getType() == EntityType.ENDERMAN && entity.getType() == EntityType.ARMOR_STAND) {
				Entity slayer = SlayerManager.getSlayerBoss();
				if (slayer != null) {
					if (slayer.getUuid().equals(getUuid()) && !LazerTimer.isRiding()) {
						LazerTimer.bossUUID = getUuid();
						LazerTimer.bossLocation = getPos();
						LazerTimer.resetTimer();
						LazerTimer.setRiding(true);
					}
				}
			}
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(CallbackInfo ci) {
		if (this.getType() == EntityType.ENDERMAN) {
			if (LazerTimer.isRiding() && LazerTimer.bossUUID.equals(getUuid()) && getVehicle() == null) {
				if (LazerTimer.remainingTime > 5.0) return;
				LazerTimer.bossUUID = null;
				LazerTimer.bossLocation = null;
				LazerTimer.setRiding(false);
			}
		}
	}
}
