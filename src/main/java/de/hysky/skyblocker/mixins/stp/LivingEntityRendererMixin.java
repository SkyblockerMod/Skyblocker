package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.stp.SkyblockerArmorTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
	private boolean skyblocker$skipHeadOnHeadRenderingIfOverriden(FeatureRenderer<T, M> featureRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if ((Utils.isOnSkyblock() || Debug.stpGlobal()) && SkyblockerConfigManager.get().uiAndVisuals.skyblockerTexturePredicates.armorTextures && featureRenderer instanceof HeadFeatureRenderer && entity instanceof LivingEntity livingEntity) {
			ItemStack headStack = livingEntity.getEquippedStack(EquipmentSlot.HEAD);

			return !(!headStack.isEmpty() && SkyblockerArmorTextures.getCustomArmorTextureLayers(headStack) != SkyblockerArmorTextures.NO_CUSTOM_TEXTURES);
		}

		return true;
	}
}
