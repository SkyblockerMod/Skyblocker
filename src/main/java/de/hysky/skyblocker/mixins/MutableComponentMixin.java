package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.hysky.skyblocker.utils.render.text.GridComponent;
import de.hysky.skyblocker.utils.render.text.GridFormattedCharSequence;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin {
	@Shadow
	public abstract ComponentContents getContents();

	@WrapOperation(method = "getVisualOrderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getVisualOrder(Lnet/minecraft/network/chat/FormattedText;)Lnet/minecraft/util/FormattedCharSequence;"))
	public FormattedCharSequence getVisualOrderText(Language instance, FormattedText formattedText, Operation<FormattedCharSequence> original) {
		if (getContents() instanceof GridComponent.Contents gridContents)
			return new GridFormattedCharSequence(gridContents);
		return original.call(instance, formattedText);
	}
}
