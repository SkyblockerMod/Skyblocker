package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.voidgloom.LazerTimer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityRideMixin {

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

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("RETURN"))
	private void onStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			if (SkyblockerConfigManager.get().slayers.endermanSlayer.lazerTimer && SlayerManager.isBossSpawned() && (Object) this instanceof EndermanEntity && entity instanceof ArmorStandEntity) {
				MobEntity slayer = SlayerManager.getSlayerEntity(EndermanEntity.class);
				if (slayer != null) {
					if (slayer.getUuid().equals(getUuid()) && !LazerTimer.isRiding()) {
						LazerTimer.BossUUID = getUuid();
						LazerTimer.BossLocation = getPos();
						LazerTimer.resetTimer();
						LazerTimer.setRiding(true);
					}
				}
			}
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(CallbackInfo ci) {
		if ((Object) this instanceof EndermanEntity) {
			if (LazerTimer.isRiding() && LazerTimer.BossUUID.equals(getUuid()) && getVehicle() == null) {
				if (LazerTimer.remainingTime > 5.0) return;
				LazerTimer.BossUUID = null;
				LazerTimer.BossLocation = null;
				LazerTimer.setRiding(false);
			}
		}
	}
}
