package de.hysky.skyblocker.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@ModifyExpressionValue(method = "updateRenderState", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;shouldRenderHitboxes()Z")), at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;invisible:Z", opcode = Opcodes.GETFIELD))
	private <E extends Entity> boolean visibleArmorStandHitBox(boolean invisible, @Local(argsOnly = true) E entity) {
		return (!(entity instanceof ArmorStandEntity) || !Utils.isOnHypixel() || !Debug.debugEnabled() || !SkyblockerConfigManager.get().debug.showInvisibleArmorStands) && invisible;
	}
}
