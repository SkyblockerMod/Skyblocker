package de.hysky.skyblocker.mixins;

import com.mojang.blaze3d.textures.TextureFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureFormat.class)
public enum TextureFormatMixin {
	SKYBLOCKER$RGBA32F(Float.BYTES * 4);

	@Shadow
	TextureFormatMixin(final int pixelSize) {}
}
