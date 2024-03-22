package de.hysky.skyblocker.mixin;


import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.auction.AuctionBrowserScreen;
import de.hysky.skyblocker.skyblock.auction.AuctionHouseScreenHandler;
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
public interface HandledScreenProviderMixin<T extends ScreenHandler> {
    @Inject(method = "open", at = @At("HEAD"), cancellable = true)
    default void skyblocker$open(Text name, ScreenHandlerType<T> type, MinecraftClient client, int id, CallbackInfo ci) {
        if (!SkyblockerConfigManager.get().general.betterPartyFinder) return;
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        T screenHandler = type.create(id, player.getInventory());
        if (!(screenHandler instanceof  GenericContainerScreenHandler containerScreenHandler)) return;
        if (PartyFinderScreen.possibleInventoryNames.contains(name.getString().toLowerCase())) {
            if (client.currentScreen != null) {
                String lowerCase = client.currentScreen.getTitle().getString().toLowerCase();
                if (lowerCase.contains("group builder")) return;
                if (lowerCase.contains("select tier")) {
                    PartyFinderScreen.isInKuudraPartyFinder = true;
                } else if (lowerCase.contains("catacombs")) {
                    PartyFinderScreen.isInKuudraPartyFinder = false;
                }
            }
            if (PartyFinderScreen.isInKuudraPartyFinder) return;
            client.player.currentScreenHandler = containerScreenHandler;
            if (client.currentScreen instanceof PartyFinderScreen screen) {
                screen.updateHandler(containerScreenHandler, name);
            } else {
                client.setScreen(new PartyFinderScreen(containerScreenHandler, player.getInventory(), name));
            }

            ci.cancel();
        } else if (name.getString().toLowerCase().contains("auctions browser")) {
            System.out.println("another one");
            AuctionHouseScreenHandler auctionHouseScreenHandler = AuctionHouseScreenHandler.of(containerScreenHandler, false);
            client.player.currentScreenHandler = auctionHouseScreenHandler;
            client.setScreen(new AuctionBrowserScreen(auctionHouseScreenHandler, client.player.getInventory()));
            ci.cancel();
        }
    }
}
