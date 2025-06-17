package de.hysky.skyblocker.mixins;

import java.util.Objects;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class RenderFishMixin {

    @ModifyReturnValue(method = "shouldRender(Lnet/minecraft/entity/projectile/FishingBobberEntity;Lnet/minecraft/client/render/Frustum;DDD)Z", at = @At("RETURN"))
    private boolean hideOtherPlayerBobbers(boolean original, @Local(argsOnly = true) FishingBobberEntity fishingBobberEntity) {
        //if rendered bobber is not the players and option to hide others is enabled do not render the bobber
        return Utils.isOnSkyblock() && SkyblockerConfigManager.get().helpers.fishing.hideOtherPlayersRods
				? original && Objects.equals(MinecraftClient.getInstance().player, fishingBobberEntity.getPlayerOwner())
				: original;
    }
}
