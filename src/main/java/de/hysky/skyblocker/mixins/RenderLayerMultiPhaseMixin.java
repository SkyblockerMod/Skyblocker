package de.hysky.skyblocker.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import de.hysky.skyblocker.utils.render.SkyblockerRenderLayers;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase.DepthTest;

@Mixin(RenderLayer.MultiPhase.class)
public class RenderLayerMultiPhaseMixin {

	@ModifyExpressionValue(method = "method_34844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderLayer$MultiPhase;ALWAYS_DEPTH_TEST:Lnet/minecraft/client/render/RenderPhase$DepthTest;", opcode = Opcodes.GETSTATIC))
	private static DepthTest skyblocker$modifyDepthTestForOutlineLayer(DepthTest depthTest) {
		return SkyblockerRenderLayers.OUTLINE_ALWAYS;
	}
}
