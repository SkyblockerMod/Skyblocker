package de.hysky.skyblocker.mixins;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.platform.InputConstants;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;

@Mixin(Screen.class)
public class ScreenMixin {
	@Shadow
	protected Minecraft minecraft;

	@Inject(method = "init(II)V", at = @At("TAIL"))
	private void skyblocker$hideCursor(CallbackInfo ci) {
		Object instance = this;

		if ((instance instanceof LevelLoadingScreen || instance instanceof ServerReconfigScreen) && Utils.isOnHypixel()) {
			//Prevents the mouse from being movable while we cancel the rendering of the screen
			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), GLFW.GLFW_CURSOR_DISABLED, this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos());
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void skyblocker$hideReconfiguringScreen(CallbackInfo ci) {
		if ((Object) this instanceof ServerReconfigScreen && Utils.isOnHypixel()) ci.cancel();
	}
}
