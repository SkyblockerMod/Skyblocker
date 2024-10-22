package de.hysky.skyblocker.mixins.rei;

import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.impl.client.gui.fabric.ScreenOverlayImplFabric;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(value = ScreenOverlayImplFabric.class, remap = false)
public class ScreenOverlayImplFabricMixin {
	@Inject(method = "lambda$renderTooltipInner$0", at = @At(target = "Lme/shedaniel/rei/api/client/gui/widgets/Tooltip$Entry;isText()Z", value = "INVOKE", shift = At.Shift.AFTER), cancellable = true, require = 0)
	private static void renderTooltipInner(Tooltip.Entry component, CallbackInfoReturnable<Stream<TooltipComponent>> cir) {
		if (!(component.getAsText() instanceof MutableText mutableText)) return;
		MutableText firstOfChain = mutableText.getFirstOfChain();
		if (firstOfChain != null) cir.setReturnValue(Stream.of(new AlignedTooltipComponent(firstOfChain)));
		else if (mutableText.getAlignedText() != null) cir.setReturnValue(Stream.of(new AlignedTooltipComponent(mutableText)));
		// The last branch of defaulting back to OrderedTextTooltipComponent is already handled by REI, so we can just omit it here
	}
}
