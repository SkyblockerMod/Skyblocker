package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

/**
 * Used for making custom GUIs
 */
@SuppressWarnings("DataFlowIssue")
public abstract class HandlerBackedScreen extends Screen {
    public GenericContainerScreenHandler getHandler() {
        return handler;
    }

    protected GenericContainerScreenHandler handler;
    protected Text inventoryName;

    public HandlerBackedScreen(Text title, Text inventoryName, GenericContainerScreenHandler handler) {
        super(title);
        this.inventoryName = inventoryName;
        this.handler = handler;
    }

    private boolean dirty = false;
    private long dirtiedTime;
    public void markDirty() {dirty = true; dirtiedTime = System.currentTimeMillis();}
    protected void unDirty() {dirty = false;}

    @Override
    public boolean shouldPause() {
        return false;
    }
    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }
    @Override
    public void removed() {
        if (this.client.player == null) {
            return;
        }
        ((ScreenHandler)this.handler).onClosed(this.client.player);
    }

    @Override
    protected void init() {
        super.init();
        markDirty();
    }

    /**
     * Called when the handler has changed, or when a slot has changed
     * If this method returns true, the screen will no longer be dirty or waiting for server
     */
    abstract protected boolean update();
    @Override
    public void tick() {
        if (!this.client.player.isAlive() || this.client.player.isRemoved()) this.client.player.closeHandledScreen();
        if (dirty && System.currentTimeMillis() - dirtiedTime > getMillisWaitTime()) {
            if (update()) {
                dirty = false;
                waitingForServer = false;
            }
        }
    }

    /**
     *
     * @return The wait time after the last slot update before calling the update method.
     * This exists due to the fact hypickle sends slots one by one sometimes.
     */
    protected int getMillisWaitTime() {return 40;}

    public boolean isDirty() {
        return dirty;
    }

    public void setWaitingForServer(boolean waitingForServer) {
        this.waitingForServer = waitingForServer;
    }

    private boolean waitingForServer = true;

    /**
     * If slotID is negative or if isWaitingForServer, the method will kindly do nothing and not click the slot
     * @param slotID the slot to click
     * @param button the mouse button to click with
     */
    public void clickAndWaitForServer(int slotID, int button) {
        if (slotID < 0 || isWaitingForServer()) return;
        assert client != null;
        assert client.interactionManager != null;
        client.interactionManager.clickSlot(handler.syncId, slotID, button, SlotActionType.PICKUP, client.player);
        waitingForServer = true;
    }
    public void clickAndWaitForServer(int slotId) { clickAndWaitForServer(slotId, 0);}

    public boolean isWaitingForServer() {
        return waitingForServer;
    }

    /**
     * Does what it says on the tin
     * @param newHandler the new handler
     * @param newTitle the new title
     */
    public void changeHandlerAndMarkDirty(GenericContainerScreenHandler newHandler, Text newTitle) {
        this.handler = newHandler;
        this.inventoryName = newTitle;
        markDirty();
    }


    /**
     * Does not draw the Inventory text
     */
    public static void renderPlayerInventory(DrawContext context, PlayerInventory playerInventory, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        ItemStack hoveredStack = null;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                int x1 = column * 18;
                int y1 = row * 18;
                ItemStack stack = playerInventory.getStack(column + row * 9 + 9);
                context.drawItem(stack, x1, y1);
                context.drawItemInSlot(textRenderer, stack, x1, y1);
                if (mouseX-x >= x1-1 && mouseX-x <= x1+17 && mouseY-y >= y1-1 && mouseY-y <= y1+17) {
                    hoveredStack = stack;
                }
            }
        }
        for (int slot = 0; slot < 9; ++slot) {
            context.drawItem(playerInventory.getStack(slot), slot * 18, 58);
            context.drawItemInSlot(textRenderer, playerInventory.getStack(slot), slot * 18, 58);
            if (mouseX-x >= slot*18-1 && mouseX-x <= slot*18+17 && mouseY-y >= 57 && mouseY-y <= 57+18) {
                hoveredStack = playerInventory.getStack(slot);
            }
        }
        context.getMatrices().pop();
        if (hoveredStack != null && !hoveredStack.isEmpty()) context.drawItemTooltip(textRenderer, hoveredStack, mouseX, mouseY);
    }

    /**
     * WHY IS THIS NOT A THING IN BASE CONTEXT GAAAAAAHHH
     */
    public static void drawCenteredStringWithoutShadow(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        context.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, false);
    }
}
