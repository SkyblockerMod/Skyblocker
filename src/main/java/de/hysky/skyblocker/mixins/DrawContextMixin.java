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

    @ModifyExpressionValue(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"))
    private <R> R skyblocker$alignedTooltip(R original, @Local(argsOnly = true) List<Text> list) {
        if (!Utils.isOnSkyblock()) return original;
        List<TooltipComponent> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Text text = list.get(i);
            List<Text> siblings = text.getSiblings();
	        if (siblings.size() >= 2) {
		        String first = siblings.getFirst().getString();
		        if (first.startsWith("@align(") && first.endsWith(")") && i + 1 < list.size()) {
                    //Some sanity checks were skipped here for brevity. Should be made sure that the string is in the correct format.
			        int x = Integer.parseInt(first.substring(7, first.length() - 1));
                    siblings.removeFirst();
			        result.add(new AlignedTooltipComponent(text.copy().asOrderedText(), x, list.get(i + 1).asOrderedText()));
                    i++;
		        } else {
                    result.add(new OrderedTextTooltipComponent(text.asOrderedText()));
                }
	        } else {
                result.add(new OrderedTextTooltipComponent(text.asOrderedText()));
            }
        }

        return (R) result;
    }
}
