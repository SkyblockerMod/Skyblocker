package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;

@Mixin(TextRenderer.class)
public interface TextRendererAccessor {

	@Invoker
	BakedGlyph invokeGetGlyph(int codePoint, Style style);
}
