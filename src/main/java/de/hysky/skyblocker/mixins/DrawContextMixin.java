package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
	@ModifyExpressionValue(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/Item;F)F"))
	private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(argsOnly = true) ItemStack stack) {
		return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
	}

	@SuppressWarnings("unchecked") //R is an ArrayList.
	@ModifyExpressionValue(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"))
	private <R> R skyblocker$alignedTooltip(R original, @Local(argsOnly = true) List<Text> list) {
		if (!Utils.isOnSkyblock()) return original;
		List<TooltipComponent> result = new ArrayList<>();

		for (Text text : list) {
			if (text instanceof MutableText mutableText) {
				MutableText firstOfChain = mutableText.getFirstOfChain();
				if (firstOfChain != null) result.add(new AlignedTooltipComponent(firstOfChain));
				else if (mutableText.getAlignedText() != null) result.add(new AlignedTooltipComponent(mutableText));
				else result.add(new OrderedTextTooltipComponent(mutableText.asOrderedText()));
			} else result.add(new OrderedTextTooltipComponent(text.asOrderedText()));
		}

		return (R) result;
	}

    @ModifyExpressionValue(method = "drawCooldownProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/ItemStack;F)F"))
    private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(argsOnly = true) ItemStack stack) {
        return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
    }
}
