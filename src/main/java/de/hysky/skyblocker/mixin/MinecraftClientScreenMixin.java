package de.hysky.skyblocker.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.hysky.skyblocker.utils.JoinWorldPlaceholderScreen;
import de.hysky.skyblocker.utils.ReconfiguringPlaceholderScreen;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientScreenMixin {

	@Shadow
	@Nullable
	public abstract ClientPlayNetworkHandler getNetworkHandler();

	//Remove Downloading Terrain Screen and Reconfiguring Screen
	@ModifyVariable(at = @At("HEAD"), method = "setScreen", ordinal = 0, argsOnly = true)
	public Screen modifySetScreen(Screen screen) {
		if (Utils.isOnSkyblock()) {
			if (screen instanceof DownloadingTerrainScreen) {
				return null;
			} else if (screen instanceof ReconfiguringScreen && this.getNetworkHandler() != null) {
				return new ReconfiguringPlaceholderScreen(this.getNetworkHandler().getConnection());
			}
		}
		return screen;
	}

	@ModifyArg(method = "joinWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;reset(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
	private Screen modifyJoinWorld(Screen screen) {
		return Utils.isOnSkyblock() ? new JoinWorldPlaceholderScreen() : screen;
	}
}
