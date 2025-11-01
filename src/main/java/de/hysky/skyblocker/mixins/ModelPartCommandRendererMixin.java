package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.command.ModelPartCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(ModelPartCommandRenderer.class)
public class ModelPartCommandRendererMixin {

	@ModifyVariable(method = "render", at = @At("LOAD"), argsOnly = true)
	private OutlineVertexConsumerProvider skyblocker$useCustomGlowConsumers(OutlineVertexConsumerProvider original, @Local OrderedRenderCommandQueueImpl.ModelPartCommand command) {
		return command.hasCustomGlow() ? GlowRenderer.getInstance().getGlowVertexConsumers() : original;
	}
}
