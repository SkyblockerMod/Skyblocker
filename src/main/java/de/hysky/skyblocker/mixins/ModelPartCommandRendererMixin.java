package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;

@Mixin(ModelPartFeatureRenderer.class)
public class ModelPartCommandRendererMixin {

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;outlineColor()I"), require = 2)
	private int skyblocker$useCustomGlowColour(SubmitNodeStorage.ModelPartSubmit command, Operation<Integer> operation) {
		return command.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? command.skyblocker$getCustomGlowColour() : operation.call(command);
	}

	@ModifyVariable(method = "render", at = @At("LOAD"), argsOnly = true, require = 2)
	private OutlineBufferSource skyblocker$useCustomGlowConsumers(OutlineBufferSource original, @Local SubmitNodeStorage.ModelPartSubmit command) {
		return command.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? GlowRenderer.getInstance().getGlowVertexConsumers() : original;
	}
}
