package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputConstants;

import de.hysky.skyblocker.skyblock.shortcut.Shortcuts;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
	@Inject(method = "click", at = @At("HEAD"))
	private static void onKeyPressed(InputConstants.Key key, CallbackInfo ci) {
		Shortcuts.onKeyPressed(key);
	}

	@Inject(method = "set", at = @At("HEAD"))
	private static void setKeyPressed(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
		Shortcuts.setKeyPressed(key, pressed);
	}
}
