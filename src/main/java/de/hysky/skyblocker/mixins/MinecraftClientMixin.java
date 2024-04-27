package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Nullable
    public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void skyblocker$handleInputEvents(CallbackInfo ci) {
        if (Utils.isOnSkyblock()) {
            HotbarSlotLock.handleInputEvents(player);
            ItemProtection.handleHotbarKeyPressed(player);
        }
    }

    //Remove Downloading Terrain Screen and Reconfiguring Screen
    @ModifyVariable(at = @At("HEAD"), method = "setScreen", ordinal = 0, argsOnly = true)
    public Screen modifySetScreen(Screen screen) {
        return switch (screen) {
            case DownloadingTerrainScreen _s when Utils.isOnHypixel() -> null;
            case ReconfiguringScreen _s when Utils.isOnHypixel() && this.getNetworkHandler() != null -> new ReconfiguringPlaceholderScreen(this.getNetworkHandler().getConnection());

            case null, default -> screen;
        };
    }

    @ModifyArg(method = "joinWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;reset(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
    private Screen modifyJoinWorld(Screen screen) {
        return Utils.isOnSkyblock() ? new JoinWorldPlaceholderScreen() : screen;
    }
}