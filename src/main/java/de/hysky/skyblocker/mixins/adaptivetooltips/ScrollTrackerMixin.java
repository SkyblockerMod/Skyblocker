package de.hysky.skyblocker.mixins.adaptivetooltips;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.utils.render.text.GridTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

@Pseudo
@Mixin(targets = "dev.isxander.adaptivetooltips.helpers.ScrollTracker")
public class ScrollTrackerMixin {

	// Targets the fourth ICONST_0/false value
	@ModifyExpressionValue(method = "isEqual", at = @At(value = "CONSTANT", args = "intValue=0", ordinal = 4), require = 0)
	private static int skyblocker$includeGridTooltipComponents(int original, @Local(name = "c1") ClientTooltipComponent tooltip1, @Local(name = "c2") ClientTooltipComponent tooltip2) {
		if (tooltip1 instanceof GridTooltipComponent grid1 && tooltip2 instanceof GridTooltipComponent grid2) {
			boolean equal = grid1.sequence().equals(grid2.sequence());

			return equal ? 1 : 0;
		}

		return original;
	}
}
