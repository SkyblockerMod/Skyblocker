package de.hysky.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.cbyrne.betterinject.annotations.Inject;
import de.hysky.skyblocker.skyblock.FishingHelper;
import de.hysky.skyblocker.skyblock.diana.MythologicalRitual;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonSecrets;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlaySound", at = @At("RETURN"))
    private void skyblocker$onPlaySound(PlaySoundS2CPacket packet) {
        FishingHelper.onSound(packet);
    }

	@ModifyVariable(method = "onItemPickupAnimation", at = @At(value = "STORE", ordinal = 0))
    private ItemEntity skyblocker$onItemPickup(ItemEntity itemEntity, @Local LivingEntity collector) {
        DungeonSecrets.onItemPickup(itemEntity, collector, collector == MinecraftClient.getInstance().player);
        return itemEntity;
    }

    @WrapWithCondition(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V", remap = false))
    private boolean skyblocker$cancelEntityPassengersWarning(Logger instance, String msg) {
        return !Utils.isOnHypixel();
    }

    @WrapWithCondition(method = "onPlayerList", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private boolean skyblocker$cancelPlayerListWarning(Logger instance, String format, Object arg) {
        return !Utils.isOnHypixel();
    }

    @WrapWithCondition(method = "onTeam", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", remap = false))
    private boolean skyblocker$cancelTeamWarning(Logger instance, String format, Object... arg) {
        return !Utils.isOnHypixel();
    }

    @Inject(method = "onParticle", at = @At("RETURN"))
    private void skyblocker$onParticle(ParticleS2CPacket packet) {
        MythologicalRitual.onParticle(packet);
    }
}
