package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.skyblock.CompactDamage;
import de.hysky.skyblocker.skyblock.HealthBars;
import de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMapTexture;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.TeleportMaze;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dwarven.CorpseFinder;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.fishing.FishingHelper;
import de.hysky.skyblocker.skyblock.fishing.FishingHookDisplayHelper;
import de.hysky.skyblocker.skyblock.fishing.SeaCreatureTracker;
import de.hysky.skyblocker.skyblock.galatea.TreeBreakProgressHud;
import de.hysky.skyblocker.skyblock.hunting.LassoHud;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.boss.demonlord.FirePillarAnnouncer;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.teleport.ResponsiveSmoothAOTE;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * All mixins in this file should be arranged in the order of the methods they inject into.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private static Logger LOGGER;

	protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "handleSetEntityData", at = @At("TAIL"))
	private void skyblocker$onEntityTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (!(entity instanceof ArmorStand armorStandEntity)) return;

		SlayerManager.checkSlayerBoss(armorStandEntity);

		if (SkyblockerConfigManager.get().slayers.blazeSlayer.firePillarCountdown != SlayersConfig.BlazeSlayer.FirePillar.OFF) FirePillarAnnouncer.checkFirePillar(entity);

		CorpseFinder.checkIfCorpse(armorStandEntity);
		HealthBars.healthBar(armorStandEntity);
		SeaCreatureTracker.onEntitySpawn(armorStandEntity);
		FishingHelper.checkIfFishWasCaught(armorStandEntity);
		TreeBreakProgressHud.onEntityUpdate(armorStandEntity);
		LassoHud.onEntityUpdate(armorStandEntity);
		try { //Prevent packet handling fails if something goes wrong so that entity trackers still update, just without compact damage numbers
			CompactDamage.compactDamage(armorStandEntity);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Compact Damage] Failed to compact damage number", e);
		}


		FishingHookDisplayHelper.onArmorStandSpawn(armorStandEntity);
	}

	@Inject(method = "handleEntityLinkPacket", at = @At("TAIL"))
	private void skyblocker$onEntityAttach(ClientboundSetEntityLinkPacket packet, CallbackInfo ci) {
		LassoHud.onEntityAttach(packet);
	}

	@Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
	private void skyblocker$beforeTeleport(ClientboundPlayerPositionPacket packet, CallbackInfo ci, @Share("playerBeforeTeleportBlockPos") LocalRef<BlockPos> beforeTeleport) {
		beforeTeleport.set(minecraft.player.blockPosition().immutable());
		ResponsiveSmoothAOTE.playerGoingToTeleport();
	}

	@Inject(method = "handleMovePlayer", at = @At(value = "RETURN"))
	private void skyblocker$onTeleport(ClientboundPlayerPositionPacket packet, CallbackInfo ci, @Share("playerBeforeTeleportBlockPos") LocalRef<BlockPos> beforeTeleport) {
		//player has been teleported by the server, tell the smooth AOTE this
		PredictiveSmoothAOTE.playerTeleported();

		TeleportMaze.INSTANCE.onTeleport(minecraft, beforeTeleport.get(), minecraft.player.blockPosition().immutable());
	}

	@Inject(method = "handleTakeItemEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;"))
	private void skyblocker$onItemPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo ci, @Local ItemEntity itemEntity) {
		DungeonManager.onItemPickup(itemEntity);
	}

	@WrapWithCondition(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V", remap = false))
	private boolean skyblocker$cancelEntityPassengersWarning(Logger instance, String msg) {
		return !Utils.isOnHypixel();
	}

	@ModifyExpressionValue(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundEntityEventPacket;getEntity(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
	private Entity skyblocker$onEntityDeath(Entity entity, @Local(argsOnly = true) ClientboundEntityEventPacket packet) {
		if (packet.getEventId() == EntityEvent.DEATH) {
			DungeonScore.handleEntityDeath(entity);
			TheEnd.onEntityDeath(entity);
		}
		return entity;
	}

	@Inject(method = "handleSetEquipment", at = @At(value = "TAIL"))
	private void skyblocker$onEntityEquip(ClientboundSetEquipmentPacket packet, CallbackInfo ci, @Local Entity entity) {
		CorpseFinder.checkIfCorpse(entity);
	}

	@Inject(method = "handleTabListCustomisation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;setFooter(Lnet/minecraft/network/chat/Component;)V"))
	private void skyblocker$updatePlayerListFooter(ClientboundTabListPacket packet, CallbackInfo ci) {
		PlayerListManager.updateFooter(packet.footer());
	}

	@WrapWithCondition(method = "handlePlayerInfoUpdate", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$cancelPlayerListWarning(Logger instance, String format, Object arg1, Object arg2) {
		return !Utils.isOnHypixel();
	}

	@Inject(method = "handleSoundEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
	private void skyblocker$onPlaySound(ClientboundSoundPacket packet, CallbackInfo ci) {
		PlaySoundEvents.FROM_SERVER.invoker().onPlaySoundFromServer(packet);
	}

	@WrapWithCondition(method = "handleUnknownCustomPayload", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$dropBadlionPacketWarnings(Logger instance, String message, Object identifier) {
		return !(Utils.isOnHypixel() && ((Identifier) identifier).getNamespace().equals("badlion"));
	}

	@WrapWithCondition(method = {"handleSetScore", "handleResetScore"}, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false), require = 2)
	private boolean skyblocker$cancelUnknownScoreboardObjectiveWarnings(Logger instance, String message, Object objectiveName) {
		return !Utils.isOnHypixel();
	}

	@WrapWithCondition(method = "handleSetPlayerTeamPacket", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", remap = false))
	private boolean skyblocker$cancelTeamWarning(Logger instance, String format, Object... arg) {
		return !Utils.isOnHypixel();
	}

	@Inject(method = "handleParticleEvent", at = @At("RETURN"))
	private void skyblocker$onParticle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
		ParticleEvents.FROM_SERVER.invoker().onParticleFromServer(packet);
	}

	@Inject(method = "handleMapItemData", at = @At("RETURN"))
	private void skyblocker$onMapItemData(ClientboundMapItemDataPacket packet, CallbackInfo ci) {
		DungeonMapTexture.onMapItemDataUpdate(packet.mapId());
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showNetworkCharts()Z"))
	private boolean shouldShowPacketSizeAndPingCharts(boolean original) {
		//make the f3+3 screen always send ping packets even when closed
		//this is needed to make smooth AOTE work so check if its enabled
		UIAndVisualsConfig.SmoothAOTE options = SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE;
		if (Utils.isOnSkyblock() && options.predictive && !PredictiveSmoothAOTE.teleportDisabled && (options.enableWeirdTransmission || options.enableEtherTransmission || options.enableInstantTransmission || options.enableSinrecallTransmission || options.enableWitherImpact)) {
			return true;
		}
		return original;
	}
}
