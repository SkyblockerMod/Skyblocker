package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.auction.AuctionHouseScreenHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractCustomHypixelGUI<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ContainerListener {

	public boolean isWaitingForServer = true;
	public AbstractCustomHypixelGUI(T handler, Inventory inventory, Component title, int imageHeight) {
		super(handler, inventory, title);
		this.imageHeight = imageHeight;
		this.inventoryLabelY = imageHeight - 94;
		handler.addSlotListener(this);
	}

	protected void clickSlot(int slotID, int button) {
		if (isWaitingForServer) return;
		if (minecraft.gameMode == null || minecraft.player == null) return;
		this.minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slotID, button, ClickType.PICKUP, minecraft.player);
		menu.getCarried().setCount(0);
		isWaitingForServer = true;
	}

	protected void clickSlot(int slotID) {
		clickSlot(slotID, 0);
	}

	public void changeHandler(AuctionHouseScreenHandler newHandler) {
		menu.removeSlotListener(this);
		((AbstractContainerScreenAccessor) this).setHandler(newHandler);
		menu.addSlotListener(this);
	}

	protected void refreshListener() {
		// Ensure this listener is added, because listeners are removed on screen removal.
		// This ensures this listener is added after a popup has been closed.
		menu.removeSlotListener(this);
		menu.addSlotListener(this);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		refreshListener();
		for (int i = 0; i < menu.getItems().size(); i++) {
			onSlotChange(menu, i, menu.getItems().get(i));
		}
	}

	@Override
	public void removed() {
		super.removed();
		menu.removeSlotListener(this);
	}

	@Override
	public final void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
		onSlotChange(this.menu, slotId, stack);
		isWaitingForServer = false;
	}

	protected abstract void onSlotChange(T handler, int slotID, ItemStack stack);

	@Override
	public void dataChanged(AbstractContainerMenu handler, int property, int value) {}
}
