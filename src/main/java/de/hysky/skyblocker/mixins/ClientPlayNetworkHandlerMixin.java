package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.skyblock.FishingHelper;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.end.BeaconHighlighter;
import de.hysky.skyblocker.skyblock.end.EnderNodes;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.waypoint.MythologicalRitual;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
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

    @Inject(method = "onBlockUpdate", at = @At("RETURN"))
    private void skyblocker$onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        if (Utils.isInTheEnd() && SlayerUtils.isInSlayer()) {
            BeaconHighlighter.beaconPositions.remove(packet.getPos());
            if (packet.getState().isOf(Blocks.BEACON)) {
                BeaconHighlighter.beaconPositions.add(packet.getPos());
            }
        }
    }

    @Inject(method = "method_37472", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V"))
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

    @WrapWithCondition(method = "onPlayerList", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private boolean skyblocker$cancelPlayerListWarning(Logger instance, String format, Object arg1, Object arg2) {
        return !Utils.isOnHypixel();
    }

    @Inject(method = "onPlaySound", at = @At("RETURN"))
    private void skyblocker$onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        FishingHelper.onSound(packet);
    }

    @WrapWithCondition(method = "warnOnUnknownPayload", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private boolean skyblocker$dropBadlionPacketWarnings(Logger instance, String message, Object identifier) {
        return !(Utils.isOnHypixel() && ((Identifier) identifier).getNamespace().equals("badlion"));
    }

    @WrapWithCondition(method = { "onScoreboardScoreUpdate", "onScoreboardScoreReset" }, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
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
        EnderNodes.onParticle(packet);
    }

    @ModifyExpressionValue(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntityStatusS2CPacket;getEntity(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"))
    private Entity skyblocker$onEntityDeath(Entity entity, @Local(argsOnly = true) EntityStatusS2CPacket packet) {
        if (packet.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            DungeonScore.handleEntityDeath(entity);
            TheEnd.onEntityDeath(entity);
        }
        return entity;
    }
}
