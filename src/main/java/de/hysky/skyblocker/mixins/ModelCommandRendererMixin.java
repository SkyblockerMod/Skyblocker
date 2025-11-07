package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(ModelCommandRenderer.class)
public class ModelCommandRendererMixin {

	@WrapOperation(method = "render(Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/OutlineVertexConsumerProvider;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand;outlineColor()I"), require = 2)
	private <S> int skyblocker$useCustomGlowColour(OrderedRenderCommandQueueImpl.ModelCommand<S> command, Operation<Integer> operation) {
		return command.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? command.skyblocker$getCustomGlowColour() : operation.call(command);
	}

	@ModifyVariable(method = "render(Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/OutlineVertexConsumerProvider;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", at = @At("LOAD"), argsOnly = true, require = 2)
	private <S> OutlineVertexConsumerProvider skyblocker$useCustomGlowConsumers(OutlineVertexConsumerProvider original, @Local(argsOnly = true) OrderedRenderCommandQueueImpl.ModelCommand<S> command) {
		return command.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? GlowRenderer.getInstance().getGlowVertexConsumers() : original;
	}
}
