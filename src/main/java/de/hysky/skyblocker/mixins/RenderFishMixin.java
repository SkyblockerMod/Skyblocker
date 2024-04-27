package de.hysky.skyblocker.mixins;


import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class RenderFishMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void skyblocker$render(FishingBobberEntity fishingBobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        //if rendered bobber is not the players and option to hide  others is enabled do not render the bobber
        if (Utils.isOnSkyblock() && fishingBobberEntity.getPlayerOwner() != MinecraftClient.getInstance().player && SkyblockerConfigManager.get().general.fishing.hideOtherPlayersRods) {
            ci.cancel();
        }
    }
}
