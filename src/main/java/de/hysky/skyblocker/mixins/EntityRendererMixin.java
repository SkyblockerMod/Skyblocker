package de.hysky.skyblocker.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@ModifyExpressionValue(method = "updateRenderState", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;shouldRenderHitboxes()Z")), at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;invisible:Z", opcode = Opcodes.GETFIELD))
	private <E extends Entity> boolean skyblocker$armorStandHitboxVisible(boolean invisible, @Local(argsOnly = true) E entity) {
		return (!(entity instanceof ArmorStandEntity) || !Utils.isOnHypixel() || !Debug.debugEnabled() || !SkyblockerConfigManager.get().debug.showInvisibleArmorStands) && invisible;
	}

	/**
	 * Vanilla infers whether an entity should glow based off it's outline colour so we do not need to
	 * do any more in this department thankfully.
	 */
	@Inject(method = "updateRenderState", at = @At("TAIL"))
	private void skyblocker$customGlow(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) EntityRenderState state) {
		boolean allowGlowInLivid = LividColor.allowGlow();
		boolean customGlow = MobGlow.hasOrComputeMobGlow(entity);
		boolean allowGlow = allowGlowInLivid && state.hasOutline() || customGlow;

		if (allowGlow && customGlow) {
			// Only apply custom flag if it doesn't have vanilla glow (so we can change Hypixel's glow colours without changing the glow's visibility)
			if (!entity.isGlowing()) {
				state.setData(MobGlow.ENTITY_HAS_CUSTOM_GLOW, true);
			}

			state.outlineColor = MobGlow.getMobGlowOrDefault(entity, MobGlow.NO_GLOW);
		} else if (!allowGlow) {
			state.outlineColor = EntityRenderState.NO_OUTLINE;
		}
	}

	// This is meant to be separate from the previous injection for organizational purposes.
	@Inject(method = "updateRenderState", at = @At(value = "TAIL"))
	private void skyblocker$mobBoundingBox(CallbackInfo ci, @Local(argsOnly = true) Entity entity) {
		boolean shouldShowBoundingBox = MobBoundingBoxes.shouldDrawMobBoundingBox(entity);

		if (shouldShowBoundingBox) {
			MobBoundingBoxes.submitBox2BeRendered(
					entity instanceof ArmorStandEntity e ? SlayerManager.getSlayerMobBoundingBox(e) : entity.getBoundingBox(),
					MobBoundingBoxes.getBoxColor(entity)
			);
		}
	}
}
