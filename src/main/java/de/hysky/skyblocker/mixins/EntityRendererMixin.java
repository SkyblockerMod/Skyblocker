package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.Boxes;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

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
		if (MobBoundingBoxes.shouldDrawMobBoundingBox(entity)) {
			MobBoundingBoxes.submitBox2BeRendered(Boxes.lerpEntityBoundingBox(entity, partialTick), MobBoundingBoxes.getBoxColor(entity));
			return;
		}

		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.HITBOX)) {
			float[] color = ColorUtils.getFloatComponents(SkyblockerConfigManager.get().slayers.highlightColor.getRGB());
			MobBoundingBoxes.submitBox2BeRendered(Boxes.lerpEntityBoundingBox(entity, partialTick), color);
		}
	}

	@ModifyReturnValue(method = "getNameTag", at = @At("RETURN"))
	private <T extends Entity> @Nullable Component skyblocker$applyCustomName(@Nullable Component original, T entity) {
		Component customName = entity.skyblocker$getCustomName();
		return customName != null ? customName : original;
	}
}
