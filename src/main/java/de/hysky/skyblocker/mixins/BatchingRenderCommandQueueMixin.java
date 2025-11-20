package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.hysky.skyblocker.injected.CustomGlowState;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.state.EntityRenderState;

@Mixin(BatchingRenderCommandQueue.class)
public class BatchingRenderCommandQueueMixin {

	// NB: Custom glow must be initialized after the record constructor is run (so that the field value is not overridden by false).

	@WrapOperation(method = { "submitModel", "submitModelPart" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/ModelCommandRenderer$Commands;add(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand;)V"),
			@At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/ModelPartCommandRenderer$Commands;add(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelPartCommand;)V")
	}, require = 2)
	private void skyblocker$markCustomGlow(@Coerce Object commandList, RenderLayer layer, @Coerce CustomGlowState command, Operation<Void> operation) {
		EntityRenderState entityStateBeingRendered = MinecraftClient.getInstance().getEntityRenderDispatcher().skyblocker$getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			command.skyblocker$setCustomGlowColour(entityStateBeingRendered.getData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR));
		}

		operation.call(commandList, layer, command);
	}

	@ModifyArg(method = "submitItem", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private Object skyblocker$markCustomGlow(Object command) {
		EntityRenderState entityStateBeingRendered = MinecraftClient.getInstance().getEntityRenderDispatcher().skyblocker$getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			((OrderedRenderCommandQueueImpl.ItemCommand) command).skyblocker$setCustomGlowColour(entityStateBeingRendered.getData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR));
		}

		return command;
	}
}
