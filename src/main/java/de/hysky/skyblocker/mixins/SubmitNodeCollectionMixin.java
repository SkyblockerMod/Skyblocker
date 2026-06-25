package de.hysky.skyblocker.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {
	@Shadow
	@Final
	public SimpleFeatureRenderPhase outline;

	@Inject(method = "submitModel", at = @At("RETURN"))
	private <S> void skyblocker$useCustomGlowRenderType(CallbackInfo ci, @Local(name = "model") Model<? super S> model, @Local(name = "state") S state, @Local(name = "renderType") RenderType renderType, @Local(name = "sprite") TextureAtlasSprite sprite, @Local(name = "pose") PoseStack.Pose pose) {
		EntityRenderState entityStateBeingRendered = Minecraft.getInstance().levelRenderer.skyblocker$getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			int customGlowColour = entityStateBeingRendered.getData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR);
			RenderType outlineRenderType = renderType.isOutline() ? renderType : renderType.skyblocker$getGlowRenderType().orElse(null);

			if (outlineRenderType != null) {
				this.outline.submit(new ModelFeatureRenderer.Submit<>(outlineRenderType, pose, model, state, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, customGlowColour, sprite, null));
			}
		}

	}

	// NB: Custom glow must be initialized after the record constructor is run (so that the field value is not overridden by false).
	@ModifyArg(method = "submitItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V"), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;outline:Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;", opcode = Opcodes.GETFIELD)))
	private SubmitNode skyblocker$markCustomGlow(SubmitNode submit) {
		EntityRenderState entityStateBeingRendered = Minecraft.getInstance().levelRenderer.skyblocker$getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			((ItemFeatureRenderer.Submit) submit).skyblocker$setCustomGlowColour(entityStateBeingRendered.getData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR));
		}

		return submit;
	}
}
