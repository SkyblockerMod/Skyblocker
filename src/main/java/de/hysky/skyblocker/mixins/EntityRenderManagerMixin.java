package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.EntityRenderMarker;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderManager.class)
public class EntityRenderManagerMixin implements EntityRenderMarker {
	@Unique
	private EntityRenderState currentStateBeingRendered;

	@Override
	@Nullable
	public EntityRenderState skyblocker$getEntityStateBeingRendered() {
		return this.currentStateBeingRendered;
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void skyblocker$markEntityStateBeingRendered(CallbackInfo ci, @Local(argsOnly = true) EntityRenderState state) {
		this.currentStateBeingRendered = state;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void skyblocker$clearEntityStateBeingRendered(CallbackInfo ci) {
		this.currentStateBeingRendered = null;
	}

	@ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
	private <E extends Entity> boolean skyblocker$dontRenderSoulweaverSkulls(boolean original, @Local(argsOnly = true) E entity) {
		return Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.hideSoulweaverSkulls && entity instanceof ArmorStandEntity armorStand && entity.isInvisible() && armorStand.hasStackEquipped(EquipmentSlot.HEAD) ? !ItemUtils.getHeadTexture(armorStand.getEquippedStack(EquipmentSlot.HEAD)).equals(HeadTextures.SOULWEAVER_HAUNTED_SKULL) : original;
	}
}
