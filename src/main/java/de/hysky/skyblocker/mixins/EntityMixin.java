package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.injected.SkyblockerEntity;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.voidgloom.LazerTimer;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin implements SkyblockerEntity {
	@Shadow
	@Final
	private EntityType<?> type;

	@Shadow
	protected UUID uuid;

	@Shadow
	public abstract boolean isInvisible();

	@Unique
	private @Nullable Component skyblocker$customName;

	@Unique
	public @Nullable Component skyblocker$getCustomName() { return skyblocker$customName; }

	@Unique
	public void skyblocker$setCustomName(Component customName) { skyblocker$customName = customName; }

	@ModifyExpressionValue(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"))
	public boolean skyblocker$showInvisibleArmorStands(boolean isSpectator, Player player) {
		return isSpectator || (isInvisible() && Utils.isOnHypixel() && Debug.debugEnabled() && SkyblockerConfigManager.get().debug.showInvisibleArmorStands && type.equals(EntityType.ARMOR_STAND));
	}

	@ModifyReturnValue(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("RETURN"))
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

	@Inject(method = "onRemoval", at = @At("TAIL"))
	private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
		if (SlayerManager.isSelectedBoss(uuid)) {
			if (SkyblockerConfigManager.get().slayers.slainTime && SlayerManager.isInSlayerQuest()) {
				SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
				if (slayerQuest != null && slayerQuest.bossSpawned && SlayerManager.isFightingOwnedSlayer()) {
					slayerQuest.bossDeathTime = Instant.now();
				}
			}
			SlayerManager.BossFight.remove();
		}
	}
}
