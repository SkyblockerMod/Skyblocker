package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInvisible()Z", ordinal = 1))
    private <E extends Entity> boolean skyblocker$armorStandHitboxVisible(boolean invisible, E entity) {
        return (!(entity instanceof ArmorStandEntity) || !Utils.isOnHypixel() || !Debug.debugEnabled() || !Debug.shouldShowInvisibleArmorStands()) && invisible;
    }
}
