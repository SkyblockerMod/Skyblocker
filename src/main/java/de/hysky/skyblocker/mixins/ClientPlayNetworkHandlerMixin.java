package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.CompactDamage;
import de.hysky.skyblocker.skyblock.FishingHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.EggFinder;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dwarven.CorpseFinder;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsChestHighlighter;
import de.hysky.skyblocker.skyblock.dwarven.WishingCompassSolver;
import de.hysky.skyblocker.skyblock.end.EnderNodes;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.demonlord.FirePillarAnnouncer;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.waypoint.MythologicalRitual;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private static Logger LOGGER;

	@Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
	private void skyblocker$onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (!(entity instanceof ArmorStandEntity armorStandEntity)) return;

		SlayerManager.checkSlayerBoss(armorStandEntity);

		if (SkyblockerConfigManager.get().slayers.blazeSlayer.firePillarCountdown != SlayersConfig.BlazeSlayer.FirePillar.OFF) FirePillarAnnouncer.checkFirePillar(entity);

		EggFinder.checkIfEgg(armorStandEntity);
		CorpseFinder.checkIfCorpse(armorStandEntity);
		try { //Prevent packet handling fails if something goes wrong so that entity trackers still update, just without compact damage numbers
			CompactDamage.compactDamage(armorStandEntity);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Compact Damage] Failed to compact damage number", e);
		}
	}

	@Inject(method = "method_64896", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V"))
	private void skyblocker$onItemDestroy(int entityId, CallbackInfo ci) {
		if (world.getEntityById(entityId) instanceof ItemEntity itemEntity) {
			DungeonManager.onItemPickup(itemEntity);
		}
	}

	@ModifyVariable(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V", ordinal = 0))
	private ItemEntity skyblocker$onItemPickup(ItemEntity itemEntity) {
		DungeonManager.onItemPickup(itemEntity);
		return itemEntity;
	}

	@WrapWithCondition(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V", remap = false))
	private boolean skyblocker$cancelEntityPassengersWarning(Logger instance, String msg) {
		return !Utils.isOnHypixel();
	}

	@ModifyExpressionValue(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntityStatusS2CPacket;getEntity(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"))
	private Entity skyblocker$onEntityDeath(Entity entity, @Local(argsOnly = true) EntityStatusS2CPacket packet) {
		if (packet.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
			DungeonScore.handleEntityDeath(entity);
			TheEnd.onEntityDeath(entity);
		}
		return entity;
	}

	@Inject(method = "onEntityEquipmentUpdate", at = @At(value = "TAIL"))
	private void skyblocker$onEntityEquip(EntityEquipmentUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
		EggFinder.checkIfEgg(entity);
		CorpseFinder.checkIfCorpse(entity);
	}

	@Inject(method = "onPlayerListHeader", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;setFooter(Lnet/minecraft/text/Text;)V"))
	private void skyblocker$updatePlayerListFooter(PlayerListHeaderS2CPacket packet, CallbackInfo ci) {
		PlayerListMgr.updateFooter(packet.footer());
	}

	@WrapWithCondition(method = "onPlayerList", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$cancelPlayerListWarning(Logger instance, String format, Object arg1, Object arg2) {
		return !Utils.isOnHypixel();
	}

	@Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
	private void skyblocker$onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
		FishingHelper.onSound(packet);
		CrystalsChestHighlighter.onSound(packet);
		SoundEvent sound = packet.getSound().value();

		// Mute Enderman sounds in the End
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().otherLocations.end.muteEndermanSounds) {
			if (sound.id().equals(SoundEvents.ENTITY_ENDERMAN_AMBIENT.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_DEATH.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_HURT.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.id()) ||
					sound.id().equals(SoundEvents.ENTITY_ENDERMAN_STARE.id())) {
				ci.cancel();
			}
		}
	}

	@WrapWithCondition(method = "warnOnUnknownPayload", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$dropBadlionPacketWarnings(Logger instance, String message, Object identifier) {
		return !(Utils.isOnHypixel() && ((Identifier) identifier).getNamespace().equals("badlion"));
	}

	@WrapWithCondition(method = {"onScoreboardScoreUpdate", "onScoreboardScoreReset"}, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$cancelUnknownScoreboardObjectiveWarnings(Logger instance, String message, Object objectiveName) {
		return !Utils.isOnHypixel();
	}

	@WrapWithCondition(method = "onTeam", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$cancelTeamWarning(Logger instance, String format, Object... arg) {
		return !Utils.isOnHypixel();
	}

	@Inject(method = "onParticle", at = @At("RETURN"))
	private void skyblocker$onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
		MythologicalRitual.onParticle(packet);
		DojoManager.onParticle(packet);
		CrystalsChestHighlighter.onParticle(packet);
		EnderNodes.onParticle(packet);
		WishingCompassSolver.onParticle(packet);
	}
}
