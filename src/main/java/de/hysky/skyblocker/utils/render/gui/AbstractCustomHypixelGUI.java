package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.auction.AuctionHouseScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public abstract class AbstractCustomHypixelGUI<T extends ScreenHandler> extends HandledScreen<T> implements ScreenHandlerListener {

    public boolean isWaitingForServer = true;
    public AbstractCustomHypixelGUI(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.addListener(this);
    }

    protected void clickSlot(int slotID, int button) {
        if (isWaitingForServer) return;
        if (client == null) return;
        assert this.client.interactionManager != null;
        this.client.interactionManager.clickSlot(handler.syncId, slotID, button, SlotActionType.PICKUP, client.player);
        handler.getCursorStack().setCount(0);
        isWaitingForServer = true;
    }

    protected void clickSlot(int slotID) {
        clickSlot(slotID, 0);
    }

    public void changeHandler(AuctionHouseScreenHandler newHandler) {
        handler.removeListener(this);
        ((HandledScreenAccessor) this).setHandler(newHandler);
        handler.addListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        handler.removeListener(this);
    }

    @Override
    public final void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        onSlotChange(this.handler, slotId, stack);
        isWaitingForServer = false;
    }

    protected abstract void onSlotChange(T handler, int slotID, ItemStack stack);

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}
}
