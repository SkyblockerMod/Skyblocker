package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.EntityRenderState;

@Mixin(BatchingRenderCommandQueue.class)
public class BatchingRenderCommandQueueMixin {

	@ModifyExpressionValue(method = "submitModelPart", at = @At(value = "NEW", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelPartCommand;"))
	private OrderedRenderCommandQueueImpl.ModelPartCommand skyblocker$markCustomGlow(OrderedRenderCommandQueueImpl.ModelPartCommand command) {
		EntityRenderState entityStateBeingRendered = MinecraftClient.getInstance().getEntityRenderDispatcher().getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_HAS_CUSTOM_GLOW, false)) {
			command.markCustomGlow();
		}

		return command;
	}

	@ModifyExpressionValue(method = "submitItem", at = @At(value = "NEW", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ItemCommand;"))
	private OrderedRenderCommandQueueImpl.ItemCommand skyblocker$markCustomGlow(OrderedRenderCommandQueueImpl.ItemCommand command) {
		EntityRenderState entityStateBeingRendered = MinecraftClient.getInstance().getEntityRenderDispatcher().getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_HAS_CUSTOM_GLOW, false)) {
			command.markCustomGlow();
		}

		return command;
	}
}
