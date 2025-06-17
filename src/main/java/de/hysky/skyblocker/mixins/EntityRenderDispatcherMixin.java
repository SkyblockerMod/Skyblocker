package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Unique
	private static final String skyblocker$SOULWEAVER_SKULL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0=";

	@ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
	private <E extends Entity> boolean skyblocker$dontRenderSoulweaverSkulls(boolean original, @Local(argsOnly = true) E entity) {
		return Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.hideSoulweaverSkulls && entity instanceof ArmorStandEntity armorStand && entity.isInvisible() && armorStand.hasStackEquipped(EquipmentSlot.HEAD) ? !ItemUtils.getHeadTexture(armorStand.getEquippedStack(EquipmentSlot.HEAD)).equals(skyblocker$SOULWEAVER_SKULL_TEXTURE) : original;
	}
}
