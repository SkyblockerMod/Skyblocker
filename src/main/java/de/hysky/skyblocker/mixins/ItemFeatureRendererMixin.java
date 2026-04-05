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
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

	@WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$ItemSubmit;outlineColor()I"), require = 3)
	private int skyblocker$useCustomGlowColour(SubmitNodeStorage.ItemSubmit submit, Operation<Integer> operation) {
		return submit.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? submit.skyblocker$getCustomGlowColour() : operation.call(submit);
	}

	@ModifyVariable(method = "renderItem", at = @At("LOAD"), name = "outlineBufferSource", require = 2)
	private OutlineBufferSource skyblocker$useCustomGlowConsumers(OutlineBufferSource original, @Local(name = "submit") SubmitNodeStorage.ItemSubmit submit) {
		return submit.skyblocker$getCustomGlowColour() != MobGlow.NO_GLOW ? GlowRenderer.getInstance().getGlowBufferSource() : original;
	}
}
