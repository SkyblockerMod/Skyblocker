package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.utils.render.text.GridFormattedCharSequence;
import de.hysky.skyblocker.utils.render.text.GridTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
	@ModifyReturnValue(method = "create(Lnet/minecraft/util/FormattedCharSequence;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("TAIL"))
	private static ClientTooltipComponent create(ClientTooltipComponent original, @Local(argsOnly = true, name = "charSequence") FormattedCharSequence charSequence) {
		if (charSequence instanceof GridFormattedCharSequence sequence) return new GridTooltipComponent(sequence);
		return original;
	}
}
