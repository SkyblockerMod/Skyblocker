package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.gui.ContainerSolverManager;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "tick", at = @At("HEAD"))
    public void skyblocker$tick(CallbackInfo ci) {
        SkyblockerMod.getInstance().onTick();
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void skyblocker$handleInputEvents(CallbackInfo ci) {
        if (Utils.isOnSkyblock) HotbarSlotLock.handleInputEvents(player);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    public void skyblocker$onSetScreen(Screen screen, CallbackInfo ci) {
        ContainerSolverManager manager = SkyblockerMod.getInstance().containerSolverManager;
        if (Utils.isOnSkyblock && screen instanceof GenericContainerScreen)
            manager.onSetScreen((GenericContainerScreen) screen);
        else
            manager.clearScreen();
    }
}