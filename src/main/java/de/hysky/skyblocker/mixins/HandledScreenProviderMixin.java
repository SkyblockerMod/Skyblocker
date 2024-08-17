package de.hysky.skyblocker.mixins;


import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.auction.AuctionBrowserScreen;
import de.hysky.skyblocker.skyblock.auction.AuctionHouseScreenHandler;
import de.hysky.skyblocker.skyblock.auction.AuctionViewScreen;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.SkyblockCraftingTableScreenHandler;
import de.hysky.skyblocker.skyblock.item.SkyblockCraftingTableScreen;
import de.hysky.skyblocker.utils.Utils;
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
	private void skyblocker$open(Text name, ScreenHandlerType<T> type, MinecraftClient client, int id, CallbackInfo ci) {
		ClientPlayerEntity player = client.player;
		if (player == null) return;
		if (!Utils.isOnSkyblock()) return;
		T screenHandler = type.create(id, player.getInventory());
		String nameLowercase = name.getString().toLowerCase();

		switch (screenHandler) {
			// Better party finder
			case GenericContainerScreenHandler ignored when SkyblockerConfigManager.get().dungeons.fancyPartyFinder && nameLowercase.contains("select tier") -> PartyFinderScreen.isInKuudraPartyFinder = true;
			case GenericContainerScreenHandler ignored when SkyblockerConfigManager.get().dungeons.fancyPartyFinder && nameLowercase.contains("catacombs") -> PartyFinderScreen.isInKuudraPartyFinder = false;

			case GenericContainerScreenHandler containerScreenHandler when SkyblockerConfigManager.get().dungeons.fancyPartyFinder && PartyFinderScreen.possibleInventoryNames.contains(nameLowercase) -> {
				if (client.currentScreen != null) {
					String lowerCase = client.currentScreen.getTitle().getString().toLowerCase();
					if (lowerCase.contains("group builder")) return;
				}

				if (PartyFinderScreen.isInKuudraPartyFinder) return;
				client.player.currentScreenHandler = containerScreenHandler;

				switch (client.currentScreen) {
					case PartyFinderScreen screen -> screen.updateHandler(containerScreenHandler, name);
					case null, default -> client.setScreen(new PartyFinderScreen(containerScreenHandler, player.getInventory(), name));
				}

				ci.cancel();
			}

			// Fancy AH
			case GenericContainerScreenHandler containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.enabled && (nameLowercase.contains("auctions browser") || nameLowercase.contains("auctions: ")) -> {
				AuctionHouseScreenHandler auctionHouseScreenHandler = AuctionHouseScreenHandler.of(containerScreenHandler, false);
				client.player.currentScreenHandler = auctionHouseScreenHandler;

				switch (client.currentScreen) {
					case AuctionBrowserScreen auctionBrowserScreen -> auctionBrowserScreen.changeHandler(auctionHouseScreenHandler);
					case null, default -> client.setScreen(new AuctionBrowserScreen(auctionHouseScreenHandler, client.player.getInventory()));
				}

				ci.cancel();
			}

			case GenericContainerScreenHandler containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.enabled && nameLowercase.contains("auction view") -> {
				AuctionHouseScreenHandler auctionHouseScreenHandler = AuctionHouseScreenHandler.of(containerScreenHandler, true);
				client.player.currentScreenHandler = auctionHouseScreenHandler;

				switch (client.currentScreen) {
					case AuctionViewScreen auctionViewScreen -> auctionViewScreen.changeHandler(auctionHouseScreenHandler);
					case null, default -> client.setScreen(new AuctionViewScreen(auctionHouseScreenHandler, client.player.getInventory(), name));
				}

				ci.cancel();
			}

			case GenericContainerScreenHandler containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.enabled && (nameLowercase.contains("confirm purchase") || nameLowercase.contains("confirm bid")) && client.currentScreen instanceof AuctionViewScreen auctionViewScreen -> {
				client.setScreen(auctionViewScreen.getConfirmPurchasePopup(name));
				client.player.currentScreenHandler = containerScreenHandler;
				ci.cancel();
			}

			// Fancy crafting table
			case GenericContainerScreenHandler containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyCraftingTable && name.getString().toLowerCase().contains("craft item") -> {
				SkyblockCraftingTableScreenHandler skyblockCraftingTableScreenHandler = new SkyblockCraftingTableScreenHandler(containerScreenHandler, player.getInventory());
				client.player.currentScreenHandler = skyblockCraftingTableScreenHandler;
				client.setScreen(new SkyblockCraftingTableScreen(skyblockCraftingTableScreenHandler, player.getInventory(), Text.literal("Craft Item")));
				ci.cancel();
			}

			case null, default -> {}
		}
	}
}
