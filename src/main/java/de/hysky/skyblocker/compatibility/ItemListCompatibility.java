package de.hysky.skyblocker.compatibility;

import com.mojang.datafixers.util.Either;
import com.operationpotato.itemlist.api.ExcludedScreensManager;
import com.operationpotato.itemlist.api.ExclusionZoneManager;
import com.operationpotato.itemlist.api.HoveredItemManager;
import com.operationpotato.itemlist.api.Plugin;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackUtils;
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;

public class ItemListCompatibility implements Plugin {

	@Override
	public void registerExclusionZones(ExclusionZoneManager zones) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		zones.addProvider(ContainerScreen.class, containerScreen -> {
			if (!SkyblockerConfigManager.get().uiAndVisuals.museumOverlay || !containerScreen.getTitle().getString().contains("Museum")) return List.of();
			AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
			return List.of(new Rect2i(accessor.getX() + accessor.getImageWidth() + 4, accessor.getY(), MuseumManager.BACKGROUND_WIDTH, MuseumManager.BACKGROUND_HEIGHT));
		});

		zones.addProvider(InventoryScreen.class, screen -> {
			if (!SkyblockerConfigManager.get().farming.plotsWidget.enabled || !Utils.isInGarden()) return List.of();
			AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
			return List.of(new Rect2i(accessor.getX() + accessor.getImageWidth() + 4, accessor.getY(), 104, 127));
		});

		zones.addProvider(Screen.class, _ -> {
			if (!VisitorHelper.shouldRender()) return List.of();
			return VisitorHelper.getExclusionZones().stream()
					.map(rect -> new Rect2i(rect.position().x(), rect.position().y(), rect.width(), rect.height()))
					.toList();
		});
	}

	@Override
	public void registerExcludedScreens(ExcludedScreensManager manager) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;

		// The Favorites Item Panel currently interferes with the side tabs of the Auction Browser Screen
		manager.addProvider(AbstractCustomHypixelGUI.class, _ -> Optional.of("Skyblocker Fancy Auction House"));
	}

	@Override
	public void registerHoveredItems(HoveredItemManager manager) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		manager.addProvider(HoveredItemStackUtils::getHoveredItemStack);
		manager.addConsumer((screen, stack, event) -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return false;

			// Wiki Lookup
			if (WikiLookupManager.handleWikiLookup(screen.getTitle().getString(), Either.right(stack), player, event)) {
				return true;
			}

			// Item Price Lookup
			if (SkyblockerConfigManager.get().helpers.itemPrice.enableItemPriceLookup && ItemPrice.ITEM_PRICE_LOOKUP.matches(event)) {
				ItemPrice.itemPriceLookup(player, stack);
				return true;
			}

			return false;
		});
	}
}
