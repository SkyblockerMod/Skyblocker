package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.utils.Boxes;
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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@ModifyExpressionValue(method = "extractRenderState", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;shouldRenderHitboxes()Z")), at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;isInvisible:Z", opcode = Opcodes.GETFIELD))
	private boolean skyblocker$armorStandHitboxVisible(boolean invisible, @Local(argsOnly = true) Entity entity) {
		return (!(entity instanceof ArmorStand) || !Utils.isOnHypixel() || !Debug.debugEnabled() || !SkyblockerConfigManager.get().debug.showInvisibleArmorStands) && invisible;
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void skyblocker$customGlow(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) EntityRenderState state) {
		boolean allowGlowInLivid = LividColor.allowGlow();
		boolean customGlow = MobGlow.hasOrComputeMobGlow(entity);
		boolean allowGlow = allowGlowInLivid && state.appearsGlowing() || customGlow;

		if (allowGlow && customGlow) {
			// Only use custom colour flag if the entity has no vanilla glow (so we can change Hypixel's glow colours without changing the glow's visibility)
			// NB: Custom glow needs to be separate to avoid weird rendering bugs.
			if (!entity.isCurrentlyGlowing()) {
				state.setData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, ARGB.opaque(MobGlow.getMobGlow(entity)));
			} else {
				state.outlineColor = ARGB.opaque(MobGlow.getMobGlow(entity));
			}
		} else if (!allowGlow) {
			state.outlineColor = EntityRenderState.NO_OUTLINE;
		}
	}

	// This is meant to be separate from the previous injection for organizational purposes.
	@Inject(method = "extractRenderState", at = @At(value = "TAIL"))
	private void skyblocker$mobBoundingBox(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) float partialTick) {
		boolean shouldShowBoundingBox = MobBoundingBoxes.shouldDrawMobBoundingBox(entity);

		if (shouldShowBoundingBox) {
			MobBoundingBoxes.submitBox2BeRendered(
					entity instanceof ArmorStand e ? SlayerManager.getSlayerMobBoundingBox(e, partialTick) : Boxes.lerpEntityBoundingBox(entity, partialTick),
					MobBoundingBoxes.getBoxColor(entity)
			);
		}
	}
}
