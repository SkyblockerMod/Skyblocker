package de.hysky.skyblocker.skyblock.tabhud.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;

public class WidgetsConfiguration extends Screen implements ScreenHandlerListener {

    private GenericContainerScreenHandler handler;

    protected WidgetsConfiguration(GenericContainerScreenHandler handler) {
        super(Text.literal("Widgets Configuration"));
        this.handler = handler;
    }

    public void updateHandler(GenericContainerScreenHandler newHandler) {
        handler = newHandler;
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}
}
