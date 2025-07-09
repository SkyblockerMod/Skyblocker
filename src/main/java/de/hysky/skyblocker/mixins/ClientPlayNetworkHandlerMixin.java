package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.CompactDamage;
import de.hysky.skyblocker.skyblock.HealthBars;
import de.hysky.skyblocker.skyblock.SmoothAOTE;
import de.hysky.skyblocker.skyblock.chocolatefactory.EggFinder;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.TeleportMaze;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dwarven.CorpseFinder;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsChestHighlighter;
import de.hysky.skyblocker.skyblock.dwarven.WishingCompassSolver;
import de.hysky.skyblocker.skyblock.end.EnderNodes;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.fishing.FishingHelper;
import de.hysky.skyblocker.skyblock.fishing.FishingHookDisplayHelper;
import de.hysky.skyblocker.skyblock.fishing.SeaCreatureTracker;
import de.hysky.skyblocker.skyblock.galatea.ForestNodes;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.demonlord.FirePillarAnnouncer;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.waypoint.MythologicalRitual;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * All mixins in this file should be arranged in the order of the methods they inject into.
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private static Logger LOGGER;

	protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
	private void skyblocker$onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (!(entity instanceof ArmorStandEntity armorStandEntity)) return;

		SlayerManager.checkSlayerBoss(armorStandEntity);

		if (SkyblockerConfigManager.get().slayers.blazeSlayer.firePillarCountdown != SlayersConfig.BlazeSlayer.FirePillar.OFF) FirePillarAnnouncer.checkFirePillar(entity);

		EggFinder.checkIfEgg(armorStandEntity);
		CorpseFinder.checkIfCorpse(armorStandEntity);
		HealthBars.healthBar(armorStandEntity);
		SeaCreatureTracker.onEntitySpawn(armorStandEntity);
		FishingHelper.checkIfFishWasCaught(armorStandEntity);
		try { //Prevent packet handling fails if something goes wrong so that entity trackers still update, just without compact damage numbers
			CompactDamage.compactDamage(armorStandEntity);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Compact Damage] Failed to compact damage number", e);
		}


		FishingHookDisplayHelper.onArmorStandSpawn(armorStandEntity);
	}

	@Inject(method = "method_64896", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V"))
	private void skyblocker$onItemDestroy(int entityId, CallbackInfo ci) {
		if (world.getEntityById(entityId) instanceof ItemEntity itemEntity) {
			DungeonManager.onItemPickup(itemEntity);
		}
	}

	@Inject(method = "onPlayerPositionLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
	private void skyblocker$beforeTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci, @Share("playerBeforeTeleportBlockPos") LocalRef<BlockPos> beforeTeleport) {
		beforeTeleport.set(client.player.getBlockPos().toImmutable());
	}

	@Inject(method = "onPlayerPositionLook", at = @At(value = "RETURN"))
	private void skyblocker$onTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci, @Share("playerBeforeTeleportBlockPos") LocalRef<BlockPos> beforeTeleport) {
		//player has been teleported by the server, tell the smooth AOTE this
		SmoothAOTE.playerTeleported();

		TeleportMaze.INSTANCE.onTeleport(client, beforeTeleport.get(), client.player.getBlockPos().toImmutable());
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
		PlayerListManager.updateFooter(packet.footer());
	}

	@WrapWithCondition(method = "onPlayerList", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$cancelPlayerListWarning(Logger instance, String format, Object arg1, Object arg2) {
		return !Utils.isOnHypixel();
	}

	@Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
	private void skyblocker$onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
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

	@WrapWithCondition(method = {"onScoreboardScoreUpdate", "onScoreboardScoreReset"}, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false), require = 2)
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
		ForestNodes.onParticle(packet);
		WishingCompassSolver.onParticle(packet);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;shouldShowPacketSizeAndPingCharts()Z"))
	private boolean shouldShowPacketSizeAndPingCharts(boolean original) {
		//make the f3+3 screen always send ping packets even when closed
		//this is needed to make smooth AOTE work so check if its enabled
		UIAndVisualsConfig.SmoothAOTE options = SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE;
		if (Utils.isOnSkyblock() && !SmoothAOTE.teleportDisabled && (options.enableWeirdTransmission || options.enableEtherTransmission || options.enableInstantTransmission || options.enableSinrecallTransmission || options.enableWitherImpact)) {
			return true;
		}
		return original;
	}
}
