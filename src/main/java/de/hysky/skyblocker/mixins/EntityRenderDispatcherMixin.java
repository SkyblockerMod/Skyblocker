package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.EntityRenderMarker;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements EntityRenderMarker {
	@Unique
	private EntityRenderState currentStateBeingRendered;

	@Override
	public @Nullable EntityRenderState skyblocker$getEntityStateBeingRendered() {
		return this.currentStateBeingRendered;
	}

	@Inject(method = "submit", at = @At("HEAD"))
	private void skyblocker$markEntityStateBeingRendered(CallbackInfo ci, @Local(argsOnly = true) EntityRenderState state) {
		this.currentStateBeingRendered = state;
	}

	@Inject(method = "submit", at = @At("RETURN"))
	private void skyblocker$clearEntityStateBeingRendered(CallbackInfo ci) {
		this.currentStateBeingRendered = null;
	}

	@ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
	private <E extends Entity> boolean skyblocker$shouldRender(boolean original, @Local(argsOnly = true) E entity) {
		// Don't render Sven Pup's Nametag
		if ((Utils.isInHub() || Utils.isInPark()) && SkyblockerConfigManager.get().slayers.wolfSlayer.hideSvenPupNametag && entity.getName().getString().contains("Sven Pup")) return false;

		// Don't render Soulweaver Skulls
		return Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.hideSoulweaverSkulls && entity instanceof ArmorStand armorStand && entity.isInvisible() && armorStand.hasItemInSlot(EquipmentSlot.HEAD) ? !ItemUtils.getHeadTexture(armorStand.getItemBySlot(EquipmentSlot.HEAD)).equals(HeadTextures.SOULWEAVER_HAUNTED_SKULL) : original;
	}
}
