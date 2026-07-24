package de.hysky.skyblocker.mixins;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {
	public ContainerScreenMixin(ChestMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@SuppressWarnings("unused")
	@Override
	protected void init() {
		super.init();

		// Museum Overlay
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.museumOverlay && this.minecraft.player != null && this.getTitle().getString().contains("Museum")) {
			int overlayWidth = MuseumManager.BACKGROUND_WIDTH; // width of the overlay
			int spacing = MuseumManager.SPACING; // space between inventory and overlay

			// Default: center inventory
			int inventoryX = (this.width - this.imageWidth) / 2;

			// If overlay would go off the right edge, shift inventory left
			if (inventoryX + this.imageWidth + spacing + overlayWidth > this.width) {
				inventoryX = this.width - (this.imageWidth + overlayWidth + spacing);
				if (inventoryX < 0) inventoryX = 0;
			}
			this.leftPos = inventoryX;

			new MuseumManager(this, this.leftPos, this.topPos, this.imageWidth);
		}
	}

	// The following two mixins are needed for the Accessory Helper & Museum Overlay

	@ModifyVariable(method = "extractBackground", at = @At("STORE"), name = "xo")
	public int x(int ignored) {
		return this.leftPos;
	}

	@ModifyVariable(method = "extractBackground", at = @At("STORE"), name = "yo")
	public int y(int ignored) {
		return this.topPos;
	}
}
