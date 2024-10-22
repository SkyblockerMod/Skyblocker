package de.hysky.skyblocker.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.mixins.accessors.DrawContextInvoker;
import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import mezz.jei.fabric.platform.RenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.stream.Stream;

@Mixin(value = RenderHelper.class, remap = false)
public abstract class RenderHelperMixin {
	@WrapMethod(method = "renderTooltip", require = 0)
	private void renderTooltip(DrawContext drawContext, List<Either<StringVisitable, TooltipData>> elements, int x, int y, TextRenderer textRenderer, ItemStack stack, Operation<Void> original) {
		List<TooltipComponent> components = elements.stream().flatMap(e -> e.map(
				text -> {
						if (text instanceof MutableText mutableText) {
							MutableText firstOfChain = mutableText.getFirstOfChain();
							if (firstOfChain != null) return Stream.of(new AlignedTooltipComponent(firstOfChain));
							else if (mutableText.getAlignedText() != null) return Stream.of(new AlignedTooltipComponent(mutableText));
						}
						return textRenderer.wrapLines(text, 400).stream().map(TooltipComponent::of);
				},
				tooltipComponent -> Stream.of(this.createClientTooltipComponent(tooltipComponent)))
		).toList();
		((DrawContextInvoker) drawContext).invokeDrawTooltip(textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
	}

	@Shadow
	protected abstract TooltipComponent createClientTooltipComponent(TooltipData tooltipComponent);
}
