package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE;
import de.hysky.skyblocker.skyblock.teleport.ResponsiveSmoothAOTE;
import de.hysky.skyblocker.utils.Boxes;
import de.hysky.skyblocker.utils.ColorUtils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@Inject(method = "extractRenderState", at = @At("TAIL"), order = 1100)
	private void skyblocker$customGlow(CallbackInfo ci, @Local(name = "entity") Entity entity, @Local(name = "state") EntityRenderState state) {
		boolean allowGlowInLivid = LividColor.allowGlow();
		boolean allowGlow = allowGlowInLivid && state.appearsGlowing();

		if (!allowGlow) {
			state.outlineColor = EntityRenderState.NO_OUTLINE;
		}
	}

	// This is meant to be separate from the previous injection for organizational purposes.
	@Inject(method = "extractRenderState", at = @At(value = "TAIL"))
	private void skyblocker$mobBoundingBox(CallbackInfo ci, @Local(name = "entity") Entity entity, @Local(name = "partialTicks") float partialTicks) {
		if (MobBoundingBoxes.shouldDrawMobBoundingBox(entity)) {
			MobBoundingBoxes.submitBox2BeRendered(Boxes.lerpEntityBoundingBox(entity, partialTicks), MobBoundingBoxes.getBoxColor(entity));
			return;
		}

		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.HITBOX)) {
			float[] color = ColorUtils.getFloatComponents(SkyblockerConfigManager.get().slayers.highlightColor.getRGB());
			MobBoundingBoxes.submitBox2BeRendered(Boxes.lerpEntityBoundingBox(entity, partialTicks), color);
		}
	}

	// This is meant to be separate from the previous injection for organizational purposes.
	@Inject(method = "extractRenderState", at = @At(value = "TAIL"))
	private void skyblocker$movePlayerRenderPos(CallbackInfo ci, @Local(name = "entity") Entity entity, @Local(name = "state") EntityRenderState state, @Local(name = "partialTicks") float partialTicks) {
		Minecraft client = Minecraft.getInstance();

		if (entity == client.player && !client.options.getCameraType().isFirstPerson()) {
			Vec3 pos;
			if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.predictive) {
				pos = PredictiveSmoothAOTE.getInterpolatedPlayerPos();

			} else {
				pos = ResponsiveSmoothAOTE.getInterpolatedPlayerPos(partialTicks);
			}
			if (pos != null)
			{
				state.x = pos.x;
				state.y = pos.y;
				state.z = pos.z;
			}
		}
	}

	@ModifyReturnValue(method = "getNameTag", at = @At("RETURN"))
	private <T extends Entity> @Nullable Component skyblocker$applyCustomName(@Nullable Component original, T entity) {
		Component customName = entity.skyblocker$getCustomName();
		return customName != null ? customName : original;
	}
}
