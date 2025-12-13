package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.ColorHelper;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@Inject(method = "updateRenderState", at = @At("TAIL"))
	private void skyblocker$customGlow(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) EntityRenderState state) {
		boolean allowGlowInLivid = LividColor.allowGlow();
		boolean customGlow = MobGlow.hasOrComputeMobGlow(entity);
		boolean allowGlow = allowGlowInLivid && state.hasOutline() || customGlow;

		if (allowGlow && customGlow) {
			// Only use custom colour flag if the entity has no vanilla glow (so we can change Hypixel's glow colours without changing the glow's visibility)
			// NB: Custom glow needs to be separate to avoid weird rendering bugs.
			if (!entity.isGlowing()) {
				state.setData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, ColorHelper.fullAlpha(MobGlow.getMobGlow(entity)));
			} else {
				state.outlineColor = ColorHelper.fullAlpha(MobGlow.getMobGlow(entity));
			}
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
