package de.hysky.skyblocker.mixin;


import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreens.Provider.class)
public interface HandledScreensMixin<T extends ScreenHandler> {
    //value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), cancellable = true
    @Inject(method = "open", at = @At("HEAD"), cancellable = true)
    default void skyblocker$open(Text name, ScreenHandlerType<T> type, MinecraftClient client, int id, CallbackInfo ci) {
        if (!SkyblockerConfigManager.get().general.betterPartyFinder) return;
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        T screenHandler = type.create(id, player.getInventory());
        if ((screenHandler instanceof GenericContainerScreenHandler containerScreenHandler) && PartyFinderScreen.possibleInventoryNames.contains(name.getString().toLowerCase())) {
            //player.sendMessage(Text.of("LESSGOOOOO " + containerScreenHandler.getRows()));
            if (client.currentScreen != null && client.currentScreen.getTitle().getString().toLowerCase().contains("group builder")) return;
            client.player.currentScreenHandler = (containerScreenHandler);
            if (client.currentScreen instanceof PartyFinderScreen screen) {
                screen.updateHandler(containerScreenHandler, name);
            } else {
                client.setScreen(new PartyFinderScreen(containerScreenHandler, player.getInventory(), name));
            }

            ci.cancel();
        }

    }
}
