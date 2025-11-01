package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.EntityRenderState;

@Mixin(ModelCommandRenderer.class)
public class ModelCommandRendererMixin {

	@ModifyVariable(method = "render(Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/OutlineVertexConsumerProvider;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", at = @At("LOAD"), argsOnly = true)
	private <S> OutlineVertexConsumerProvider skyblocker$useCustomGlowConsumers(OutlineVertexConsumerProvider original, @Local(argsOnly = true) OrderedRenderCommandQueueImpl.ModelCommand<S> model) {
		return model.state() instanceof EntityRenderState entityState && entityState.getDataOrDefault(MobGlow.ENTITY_HAS_CUSTOM_GLOW, false) ? GlowRenderer.getInstance().getGlowVertexConsumers() : original;
	}
}
