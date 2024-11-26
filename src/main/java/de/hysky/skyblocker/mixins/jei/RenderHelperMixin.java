package de.hysky.skyblocker.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.fabric.platform.RenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = RenderHelper.class, remap = false)
public abstract class RenderHelperMixin {
	@WrapOperation(method = "lambda$renderTooltip$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;wrapLines(Lnet/minecraft/text/StringVisitable;I)Ljava/util/List;"), require = 0)
	private static List<OrderedText> skyblocker$renderTooltip(TextRenderer instance, StringVisitable text, int width, Operation<List<OrderedText>> original) {
		if (text instanceof MutableText mutableText && mutableText.getXOffset() != Integer.MIN_VALUE) return List.of(mutableText.asOrderedText());
		return original.call(instance, text, width);
	}
}
