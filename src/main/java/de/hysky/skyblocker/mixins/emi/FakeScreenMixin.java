package de.hysky.skyblocker.mixins.emi;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
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

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Mixin(value = FakeScreen.class)
public class FakeScreenMixin {
	@ModifyReceiver(method = "getTooltipComponentListFromItem", at = @At(target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;", value = "INVOKE"), require = 0)
	private <A> Stream<TooltipComponent> skyblocker$alignedTooltip(Stream<TooltipComponent> instance, Collector<? super TooltipComponent, A, List<TooltipComponent>> arCollector, @Local(argsOnly = true) ItemStack stack) {
		return Screen.getTooltipFromItem(MinecraftClient.getInstance(), stack).stream().map(text -> {
			if (text instanceof MutableText mutableText) {
				MutableText firstOfChain = mutableText.getFirstOfChain();
				if (firstOfChain != null) return new AlignedTooltipComponent(firstOfChain);
				else if (mutableText.getAlignedText() != null) return new AlignedTooltipComponent(mutableText);
			} return new OrderedTextTooltipComponent(text.asOrderedText());
		});
	}
}
