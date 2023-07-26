package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import dev.cbyrne.betterinject.annotations.Inject;
import me.xmrvizzy.skyblocker.skyblock.FishingHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onPlaySound", at = @At("RETURN"))
    private void skyblocker$onPlaySound(PlaySoundS2CPacket packet) {
        FishingHelper.onSound(packet);
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
}
