package de.hysky.skyblocker.mixins.emi;

import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import dev.emi.emi.screen.FakeScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FakeScreen.class, remap = false)
public class FakeScreenMixin {
	@Inject(method = "getTooltipComponentListFromItem", at = @At(target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;", value = "INVOKE", shift = At.Shift.AFTER), require = 0, cancellable = true)
	private void skyblocker$alignedTooltip(ItemStack stack, CallbackInfoReturnable<List<TooltipComponent>> cir) {
		cir.setReturnValue(Screen.getTooltipFromItem(MinecraftClient.getInstance(), stack).stream().map(text -> {
			if (text instanceof MutableText mutableText) {
				MutableText firstOfChain = mutableText.getFirstOfChain();
				if (firstOfChain != null) return new AlignedTooltipComponent(firstOfChain);
				else if (mutableText.getAlignedText() != null) return new AlignedTooltipComponent(mutableText);
				else return new OrderedTextTooltipComponent(mutableText.asOrderedText());
			} else return new OrderedTextTooltipComponent(text.asOrderedText());
		}).toList());
	}
}
