package de.hysky.skyblocker.mixins.accessors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsInvoker {

	@Invoker
	void invokeSubmitColoredRectangle(RenderPipeline pipeline, TextureSetup textureSetup, int x0, int y0, int x1, int y1, int colour1, @Nullable Integer colour2);
}
