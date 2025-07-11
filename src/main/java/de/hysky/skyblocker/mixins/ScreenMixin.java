package de.hysky.skyblocker.mixins;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;

@Mixin(Screen.class)
public class ScreenMixin {
	@Shadow
	protected MinecraftClient client;

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
	private void hideCursor(CallbackInfo ci) {
		Object instance = (Object) this;

		if ((instance instanceof DownloadingTerrainScreen || instance instanceof ReconfiguringScreen) && Utils.isOnHypixel()) {
			//Prevents the mouse from being movable while we cancel the rendering of the screen
			InputUtil.setCursorParameters(this.client.getWindow().getHandle(), GLFW.GLFW_CURSOR_DISABLED, this.client.mouse.getX(), this.client.mouse.getY());
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void hideReconfiguringScreen(CallbackInfo ci) {
		if ((Object) this instanceof ReconfiguringScreen && Utils.isOnHypixel()) ci.cancel();
	}
}
