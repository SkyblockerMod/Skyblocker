package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	@ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
	private <E extends Entity> boolean skyblocker$dontRenderSoulweaverSkulls(boolean original, @Local(argsOnly = true) E entity) {
		return Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.hideSoulweaverSkulls && entity instanceof ArmorStandEntity armorStand && entity.isInvisible() && armorStand.hasStackEquipped(EquipmentSlot.HEAD) ? !ItemUtils.getHeadTexture(armorStand.getEquippedStack(EquipmentSlot.HEAD)).equals(HeadTextures.SOULWEAVER_HAUNTED_SKULL) : original;
	}
}
