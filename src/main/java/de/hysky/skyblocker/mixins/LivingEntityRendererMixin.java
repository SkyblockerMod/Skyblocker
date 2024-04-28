package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @ModifyExpressionValue(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;isVisible(Lnet/minecraft/entity/LivingEntity;)Z"))
    private <T extends LivingEntity> boolean skyblocker$armorStandVisible(boolean visible, T entity) {
        return entity instanceof ArmorStandEntity && Utils.isOnHypixel() && Debug.debugEnabled() && Debug.shouldShowInvisibleArmorStands() || visible;
    }
}
