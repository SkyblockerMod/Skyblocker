package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class RenderFishMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void skyblocker$render(CallbackInfo ci, @Local(argsOnly = true) FishingBobberEntity fishingBobberEntity) {
        //if rendered bobber is not the players and option to hide  others is enabled do not render the bobber
        if (Utils.isOnSkyblock() && fishingBobberEntity.getPlayerOwner() != MinecraftClient.getInstance().player && SkyblockerConfigManager.get().helpers.fishing.hideOtherPlayersRods) {
            ci.cancel();
        }
    }
}
