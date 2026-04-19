package de.hysky.skyblocker.mixins;

import org.lwjgl.opengl.GL33;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.textures.TextureFormat;

@Mixin(GlConst.class)
public class GlConstMixin {

	@Inject(method = "toGlInternalId(Lcom/mojang/blaze3d/textures/TextureFormat;)I", at = @At("HEAD"), cancellable = true)
	private static void skyblocker$handleCustomFormat(TextureFormat format, CallbackInfoReturnable<Integer> cir) {
		if (format == TextureFormat.SKYBLOCKER$RGBA32F) {
			cir.setReturnValue(GL33.GL_RGBA32F);
		}
	}
}
