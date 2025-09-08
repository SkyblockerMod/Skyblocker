package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.shortcut.Shortcuts;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
	@Inject(method = "onKeyPressed", at = @At("HEAD"))
	private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
		Shortcuts.onKeyPressed(key);
	}

	@Inject(method = "setKeyPressed", at = @At("HEAD"))
	private static void setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
		Shortcuts.setKeyPressed(key, pressed);
	}
}
