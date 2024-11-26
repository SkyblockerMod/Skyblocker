package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.utils.render.gui.AlignedOrderedText;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(TextHandler.class)
public abstract class TextHandlerMixin {
	@Final
	@Shadow
	TextHandler.WidthRetriever widthRetriever;

	@WrapOperation(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/OrderedText;accept(Lnet/minecraft/text/CharacterVisitor;)Z"))
	private boolean skyblocker$getWidth(OrderedText instance, CharacterVisitor characterVisitor, Operation<Boolean> original, @Local MutableFloat mutableFloat) {
		if (instance instanceof AlignedOrderedText alignedOrderedText) {
			MutableFloat width = new MutableFloat();
			List<AlignedOrderedText.Segment> segments = alignedOrderedText.segments();
			float tmp = 0;
			for (AlignedOrderedText.Segment segment : segments) {
				tmp = segment.xOffset();
				if (width.addAndGet(-tmp) < 0) width.setValue(tmp); // If the offset is greater than the current width, set the width to the offset to not allow clipping between segments
				segment.text().accept((index, style, codePoint) -> { // This is a copied version of the original operation, but the width addition is done to our MutableFloat rather than the original
					width.add(this.widthRetriever.getWidth(codePoint, style));
					return true;
				});
			}

			mutableFloat.setValue(width.getValue());
			return true;
		}
		return original.call(instance, characterVisitor);
	}

	@Inject(method = "wrapLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/text/Style;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
	private void skyblocker$wrapLines(StringVisitable text, int maxWidth, Style style, BiConsumer<StringVisitable, Boolean> lineConsumer, CallbackInfo ci) {
		if (text instanceof MutableText mutableText) {
			switch (mutableText.getXOffset()) {
				case 0 -> {
					lineConsumer.accept(mutableText, false);
					ci.cancel();
				}
				case Integer.MIN_VALUE -> {}
				default -> {
					lineConsumer.accept(mutableText.getFirstOfChain(), false);
					ci.cancel();
				}
			}
		}
	}
}
