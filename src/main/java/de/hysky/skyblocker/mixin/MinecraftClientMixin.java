package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.utils.JoinWorldPlaceholderScreen;
import de.hysky.skyblocker.utils.ReconfiguringPlaceholderScreen;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import dev.cbyrne.betterinject.annotations.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    protected abstract void reset(Screen screen);

    @Shadow
    @Nullable
    public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void skyblocker$handleInputEvents() {
        if (Utils.isOnSkyblock()) {
            HotbarSlotLock.handleInputEvents(player);
        }
    }

    //Remove Downloading Terrain Screen and Reconfiguring Screen
    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void setScreen(final Screen screen, final CallbackInfo ci) {
        if (Utils.isOnSkyblock()) {
            if (screen instanceof DownloadingTerrainScreen) {
                ci.cancel();
                this.setScreen(null);
            } else if (screen instanceof ReconfiguringScreen && this.getNetworkHandler() != null) {
                ci.cancel();
                this.setScreen(new ReconfiguringPlaceholderScreen(this.getNetworkHandler().getConnection()));
            }
        }
    }

    @Redirect(method = "joinWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;reset(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void joinWorld(final MinecraftClient minecraft, final Screen screen) {
        if (Utils.isOnSkyblock()) {
            this.reset(new JoinWorldPlaceholderScreen());
        }
    }
}