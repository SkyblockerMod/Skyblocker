package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Function;

@Mixin(DrawContext.class)
public interface DrawContextInvoker {

    @Invoker
    void invokeDrawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture);

    @Invoker
    void invokeDrawTexturedQuad(Function<Identifier, RenderLayer> renderLayers, Identifier texture, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2, int color);
}
