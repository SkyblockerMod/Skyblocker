package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WidgetsConfigurationScreen extends Screen implements ScreenHandlerListener {

    private GenericContainerScreenHandler handler;

    // Tabs and stuff
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    private WidgetsOrderingTab widgetsOrderingTab;

    private boolean switchingToPopup = false;

    public WidgetsConfigurationScreen(GenericContainerScreenHandler handler) {
        super(Text.literal("Widgets Configuration"));
        this.handler = handler;
        handler.addListener(this);
    }

    @Override
    protected void init() {
        widgetsOrderingTab = new WidgetsOrderingTab(this.client, this.handler);
        this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width)
                .tabs(this.widgetsOrderingTab)
                .build();
        this.tabNavigation.selectTab(0, false);
        this.addDrawableChild(tabNavigation);
        switchingToPopup = false;
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        if (this.tabNavigation != null) {
            this.tabNavigation.setWidth(this.width);
            this.tabNavigation.init();
            int i = this.tabNavigation.getNavigationFocus().getBottom();
            ScreenRect screenRect = new ScreenRect(0, i, this.width, this.height - i - 20 /* A bit of a footer */);
            this.tabManager.setTabArea(screenRect);
        }
    }

    public void updateHandler(GenericContainerScreenHandler newHandler) {
        handler.removeListener(this);
        handler = newHandler;
        handler.addListener(this);
        widgetsOrderingTab.updateHandler(handler);
    }

    private @Nullable ItemStack slotThirteenBacklog = null;

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        if (widgetsOrderingTab == null) {
            if (slotId == 13) slotThirteenBacklog = stack.copy();
            return;
        }
        if (slotId == 13) {
            if (stack.isOf(Items.HOPPER)) {
                widgetsOrderingTab.hopper(ItemUtils.getLore(stack));
            } else {
                widgetsOrderingTab.hopper(null);
            }
        }
        if (slotId > 9 && slotId < this.handler.getRows() * 9 - 9) {
            widgetsOrderingTab.updateEntries();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (slotThirteenBacklog != null && widgetsOrderingTab != null) {
            widgetsOrderingTab.hopper(ItemUtils.getLore(slotThirteenBacklog));
            slotThirteenBacklog = null;
        }
        if (!this.client.player.isAlive() || this.client.player.isRemoved()) {
            this.client.player.closeHandledScreen();
        }
    }

    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

    @Override
    public void removed() {
        if (!switchingToPopup && this.client != null && this.client.player != null) {
            this.handler.onClosed(this.client.player);
        }
        handler.removeListener(this);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
