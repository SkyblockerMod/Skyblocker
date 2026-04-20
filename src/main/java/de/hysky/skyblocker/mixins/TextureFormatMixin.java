package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.textures.TextureFormat;

@Mixin(TextureFormat.class)
public enum TextureFormatMixin {
	SKYBLOCKER$RGBA32F(Float.BYTES * 4);

	@Shadow
	TextureFormatMixin(final int pixelSize) {}
}
