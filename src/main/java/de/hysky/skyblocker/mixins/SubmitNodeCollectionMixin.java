package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.hysky.skyblocker.injected.CustomGlowState;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {

	// NB: Custom glow must be initialized after the record constructor is run (so that the field value is not overridden by false).

	@WrapOperation(method = { "submitModel", "submitModelPart" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;)V"),
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelPartFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;)V")
	}, require = 2)
	private void skyblocker$markCustomGlow(@Coerce Object commandList, RenderType layer, @Coerce CustomGlowState command, Operation<Void> operation) {
		EntityRenderState entityStateBeingRendered = Minecraft.getInstance().levelRenderer.skyblocker$getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			command.skyblocker$setCustomGlowColour(entityStateBeingRendered.getData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR));
		}

		operation.call(commandList, layer, command);
	}

	@ModifyArg(method = "submitItem", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private Object skyblocker$markCustomGlow(Object command) {
		EntityRenderState entityStateBeingRendered = Minecraft.getInstance().levelRenderer.skyblocker$getEntityStateBeingRendered();

		if (entityStateBeingRendered != null && entityStateBeingRendered.getDataOrDefault(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR, MobGlow.NO_GLOW) != MobGlow.NO_GLOW) {
			((SubmitNodeStorage.ItemSubmit) command).skyblocker$setCustomGlowColour(entityStateBeingRendered.getData(MobGlow.ENTITY_CUSTOM_GLOW_COLOUR));
		}

		return command;
	}
}
