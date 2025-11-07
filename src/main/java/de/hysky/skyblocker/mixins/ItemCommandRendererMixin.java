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
import net.minecraft.client.render.command.ItemCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

@Mixin(ItemCommandRenderer.class)
public class ItemCommandRendererMixin {

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ItemCommand;outlineColor()I"), require = 2)
	private int skyblocker$useCustomGlowColour(OrderedRenderCommandQueueImpl.ItemCommand command, Operation<Integer> operation) {
		return command.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? command.skyblocker$getCustomGlowColour() : operation.call(command);
	}

	@ModifyVariable(method = "render", at = @At("LOAD"), argsOnly = true, require = 2)
	private OutlineVertexConsumerProvider skyblocker$useCustomGlowConsumers(OutlineVertexConsumerProvider original, @Local OrderedRenderCommandQueueImpl.ItemCommand command) {
		return command.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? GlowRenderer.getInstance().getGlowVertexConsumers() : original;
	}
}
