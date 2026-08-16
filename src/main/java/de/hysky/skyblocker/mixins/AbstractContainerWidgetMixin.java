package de.hysky.skyblocker.mixins;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Mixin(AbstractContainerWidget.class)
public class AbstractContainerWidgetMixin {
	@Shadow
	private @Nullable GuiEventListener focused;

	@Inject(method = "setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V", at = @At("HEAD"), cancellable = true)
	private void onSetFocused(GuiEventListener focused, CallbackInfo ci) {
		if (focused == this.focused) ci.cancel(); // AbstractContainerEventHandler does this check but not this.
	}
}
