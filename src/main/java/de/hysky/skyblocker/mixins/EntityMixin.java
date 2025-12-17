package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.voidgloom.LazerTimer;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	@Final
	private EntityType<?> type;

	@Shadow
	protected UUID uuid;

	@Shadow
	public abstract boolean isInvisible();

	@ModifyExpressionValue(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
	public boolean skyblocker$showInvisibleArmorStands(boolean isSpectator, PlayerEntity player) {
		return isSpectator || (isInvisible() && Utils.isOnHypixel() && Debug.debugEnabled() && SkyblockerConfigManager.get().debug.showInvisibleArmorStands && type.equals(EntityType.ARMOR_STAND));
	}

	@ModifyReturnValue(method = "startRiding(Lnet/minecraft/entity/Entity;ZZ)Z", at = @At("RETURN"))
	private boolean modifyStartRidingReturnValue(boolean originalReturnValue, Entity entity, boolean force) {
		if (originalReturnValue && SkyblockerConfigManager.get().slayers.endermanSlayer.lazerTimer &&
				type == EntityType.ENDERMAN &&
				entity.getType() == EntityType.ARMOR_STAND &&
				SlayerManager.isSelectedBoss(uuid) &&
				!LazerTimer.isActive()) LazerTimer.activate();
		return originalReturnValue;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(CallbackInfo ci) {
		if (type == EntityType.ENDERMAN && SkyblockerConfigManager.get().slayers.endermanSlayer.lazerTimer &&
				SlayerManager.isSelectedBoss(uuid) && LazerTimer.isActive()) LazerTimer.tick();
	}

	@Inject(method = "onRemove", at = @At("TAIL"))
	private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
		if (SlayerManager.isSelectedBoss(uuid)) {
			if (SkyblockerConfigManager.get().slayers.slainTime && SlayerManager.isInSlayer()) {
				SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
				SlayerManager.BossFight bossFight = SlayerManager.getBossFight();
				if (slayerQuest != null && slayerQuest.bossSpawned && bossFight != null && bossFight.playerBoss) {
					slayerQuest.bossDeathTime = Instant.now();
				}
			}
			SlayerManager.BossFight.remove();
		}
	}
}
