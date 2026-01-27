package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.auction.AuctionBrowserScreen;
import de.hysky.skyblocker.skyblock.auction.AuctionHouseScreenHandler;
import de.hysky.skyblocker.skyblock.auction.AuctionViewScreen;
import de.hysky.skyblocker.skyblock.dungeon.LeapOverlay;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.SkyblockCraftingTableScreenHandler;
import de.hysky.skyblocker.skyblock.item.SkyblockCraftingTableScreen;
import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import de.hysky.skyblocker.skyblock.radialMenu.RadialMenuManager;
import de.hysky.skyblocker.skyblock.radialMenu.RadialMenuScreen;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Utils;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuScreens.ScreenConstructor.class)
public interface MenuScreensConstructorMixin<T extends AbstractContainerMenu> {

	@Inject(method = "fromPacket", at = @At("HEAD"), cancellable = true)
	private void skyblocker$open(Component name, MenuType<T> type, Minecraft client, int id, CallbackInfo ci) {
		LocalPlayer player = client.player;
		if (player == null) return;
		if (!Utils.isOnSkyblock()) return;
		T screenHandler = type.create(id, player.getInventory());
		String nameLowercase = name.getString().trim().toLowerCase(Locale.ENGLISH);

		switch (screenHandler) {
			// Better party finder
			case ChestMenu ignored when SkyblockerConfigManager.get().dungeons.fancyPartyFinder && nameLowercase.startsWith("select tier") -> PartyFinderScreen.isInKuudraPartyFinder = true;
			case ChestMenu ignored when SkyblockerConfigManager.get().dungeons.fancyPartyFinder && nameLowercase.startsWith("catacombs") -> PartyFinderScreen.isInKuudraPartyFinder = false;

			case ChestMenu containerScreenHandler when SkyblockerConfigManager.get().dungeons.fancyPartyFinder && PartyFinderScreen.possibleInventoryNames.contains(nameLowercase) -> {
				if (client.screen != null) {
					String lowerCase = client.screen.getTitle().getString().toLowerCase(Locale.ENGLISH);
					if (lowerCase.contains("group builder")) return;
				}

				if (PartyFinderScreen.isInKuudraPartyFinder) return;
				client.player.containerMenu = containerScreenHandler;

				switch (client.screen) {
					case PartyFinderScreen screen -> screen.updateHandler(containerScreenHandler, name);
					case null, default -> client.setScreen(new PartyFinderScreen(containerScreenHandler, player.getInventory(), name));
				}

				ci.cancel();
			}

			// Fancy AH
			case ChestMenu containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.enabled && (nameLowercase.equals("auctions browser") || nameLowercase.startsWith("auctions: ")) -> {
				AuctionHouseScreenHandler auctionHouseScreenHandler = AuctionHouseScreenHandler.of(containerScreenHandler, false);
				client.player.containerMenu = auctionHouseScreenHandler;

				switch (client.screen) {
					case AuctionBrowserScreen auctionBrowserScreen -> auctionBrowserScreen.changeHandler(auctionHouseScreenHandler);
					case null, default -> client.setScreen(new AuctionBrowserScreen(auctionHouseScreenHandler, client.player.getInventory()));
				}

				ci.cancel();
			}

			case ChestMenu containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.enabled && nameLowercase.endsWith("auction view") -> {
				AuctionHouseScreenHandler auctionHouseScreenHandler = AuctionHouseScreenHandler.of(containerScreenHandler, true);
				client.player.containerMenu = auctionHouseScreenHandler;

				switch (client.screen) {
					case AuctionViewScreen auctionViewScreen -> auctionViewScreen.changeHandler(auctionHouseScreenHandler);
					case null, default -> client.setScreen(new AuctionViewScreen(auctionHouseScreenHandler, client.player.getInventory(), name));
				}

				ci.cancel();
			}

			case ChestMenu containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.enabled && (nameLowercase.equals("confirm purchase") || nameLowercase.equals("confirm bid")) && client.screen instanceof AuctionViewScreen auctionViewScreen -> {
				client.setScreen(auctionViewScreen.getConfirmPurchasePopup(name));
				client.player.containerMenu = containerScreenHandler;
				ci.cancel();
			}

			// Fancy crafting table
			case ChestMenu containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.fancyCraftingTable && name.getString().toLowerCase(Locale.ENGLISH).contains("craft item") -> {
				SkyblockCraftingTableScreenHandler skyblockCraftingTableScreenHandler = new SkyblockCraftingTableScreenHandler(containerScreenHandler, player.getInventory());
				client.player.containerMenu = skyblockCraftingTableScreenHandler;
				client.setScreen(new SkyblockCraftingTableScreen(skyblockCraftingTableScreenHandler, player.getInventory(),
						ResourcePackCompatibility.options.renameCraftingTable().orElse(false) ? Component.literal("CraftingTableSkyblocker") : Component.literal("Craft Item")));
				ci.cancel();
			}

			// Excessive widgets config
			case ChestMenu containerScreenHandler when SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled && WidgetsConfigurationScreen.overrideWidgetsScreen && (nameLowercase.startsWith("widgets in") || nameLowercase.startsWith("widgets on") || nameLowercase.equals("tablist widgets") || (nameLowercase.endsWith("widget settings") && !nameLowercase.startsWith("reset")) || (nameLowercase.startsWith("shown") && client.screen instanceof WidgetsConfigurationScreen)) -> {
				client.player.containerMenu = containerScreenHandler;
				switch (client.screen) {
					case WidgetsConfigurationScreen screen -> screen.updateHandler(containerScreenHandler, nameLowercase);
					case null, default -> client.setScreen(new WidgetsConfigurationScreen(containerScreenHandler, nameLowercase));
				}
				ci.cancel();
			}

			// Leap Overlay
			case ChestMenu containerScreenHandler when Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.leapOverlay.enableLeapOverlay && nameLowercase.contains(LeapOverlay.TITLE.toLowerCase(Locale.ENGLISH)) -> {
				client.player.containerMenu = containerScreenHandler;
				client.setScreen(new LeapOverlay(containerScreenHandler));

				ci.cancel();
			}

			// radial menus
			case ChestMenu containerScreenHandler when RadialMenuManager.isMenuExistsFromTitle(nameLowercase) -> {
				client.player.containerMenu = containerScreenHandler;
				RadialMenu menuType = RadialMenuManager.getMenuFromTitle(nameLowercase);
				client.setScreen(new RadialMenuScreen(containerScreenHandler, menuType, name));

				ci.cancel();
			}

			case null, default -> {}
		}
	}
}
