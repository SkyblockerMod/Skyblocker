package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;

@Mixin(TextRenderer.class)
public interface TextRendererAccessor {

	@Accessor
	boolean getValidateAdvance();

	@Invoker
	FontStorage invokeGetFontStorage(Identifier id);
}
