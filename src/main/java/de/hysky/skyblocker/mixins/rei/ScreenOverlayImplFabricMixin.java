package de.hysky.skyblocker.mixins.rei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.shedaniel.rei.impl.client.gui.fabric.ScreenOverlayImplFabric;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = ScreenOverlayImplFabric.class)
public class ScreenOverlayImplFabricMixin {
	@WrapOperation(method = "lambda$renderTooltipInner$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextHandler;wrapLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/text/Style;)Ljava/util/List;"), require = 0)
	private static List<StringVisitable> renderTooltipInner(TextHandler instance, StringVisitable text, int maxWidth, Style style, Operation<List<StringVisitable>> original) {
		if (text instanceof MutableText mutableText && mutableText.getXOffset() != Integer.MIN_VALUE) return List.of();

		return original.call(instance, text, maxWidth, style);
	}
}
