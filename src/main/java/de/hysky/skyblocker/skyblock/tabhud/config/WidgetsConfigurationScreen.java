package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WidgetsConfigurationScreen extends Screen implements ScreenHandlerListener {

    private GenericContainerScreenHandler handler;
    private String titleLowercase;

    private boolean tabPreview = false;
    private PreviewTab previewTab;

    private final Map<String, Location> nameToLocation = Map.ofEntries(
            Map.entry("private islands", Location.PRIVATE_ISLAND),
            Map.entry("the hub", Location.HUB),
            Map.entry("the dungeon hub", Location.DUNGEON_HUB),
            Map.entry("the farming islands", Location.THE_FARMING_ISLAND),
            Map.entry("garden", Location.GARDEN),
            Map.entry("the park", Location.THE_PARK),
            Map.entry("the gold mine", Location.GOLD_MINE),
            Map.entry("deep caverns", Location.DEEP_CAVERNS),
            Map.entry("dwarven mines", Location.DWARVEN_MINES),
            Map.entry("crystal hollows", Location.CRYSTAL_HOLLOWS),
            Map.entry("the mineshaft", Location.GLACITE_MINESHAFT),
            Map.entry("spider's den", Location.SPIDERS_DEN),
            Map.entry("the end", Location.THE_END),
            Map.entry("crimson isle", Location.CRIMSON_ISLE),
            Map.entry("kuudra", Location.KUUDRAS_HOLLOW),
            Map.entry("the rift", Location.THE_RIFT),
            Map.entry("jerry's workshop", Location.WINTER_ISLAND)
    );
    private Location currentLocation = Location.HUB;

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public boolean isPreviewVisible() {
        return tabPreview;
    }

    // Tabs and stuff
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    @Nullable
    private TabNavigationWidget tabNavigation;
    private WidgetsOrderingTab widgetsOrderingTab;

    private boolean switchingToPopup = false;

    public WidgetsConfigurationScreen(GenericContainerScreenHandler handler, String titleLowercase) {
        super(Text.literal("Widgets Configuration"));
        this.handler = handler;
        handler.addListener(this);
        this.titleLowercase = titleLowercase;
    }

    @Override
    protected void init() {
        widgetsOrderingTab = new WidgetsOrderingTab(this.client, this.handler);
        previewTab = new PreviewTab(this.client, this);
        this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width)
                .tabs(this.widgetsOrderingTab, this.previewTab)
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

    public void updateHandler(GenericContainerScreenHandler newHandler, String titleLowercase) {
        handler.removeListener(this);
        handler = newHandler;
        handler.addListener(this);
        this.titleLowercase = titleLowercase;
        String trim = this.titleLowercase
                .replace("widgets in", "")
                .replace("widgets on", "")
                .trim();

        currentLocation = nameToLocation.getOrDefault(trim, Utils.getLocation());
        widgetsOrderingTab.updateHandler(handler);
    }

    public GenericContainerScreenHandler getHandler() {
        return handler;
    }

    private @Nullable ItemStack slotThirteenBacklog = null;

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        if (slotId == 4) {
            tabPreview = stack.isOf(Items.PLAYER_HEAD);
        }
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
        if (slotId > 9 && slotId < this.handler.getRows() * 9 - 9 || slotId == 45 || slotId == 53) {
            widgetsOrderingTab.updateEntries(titleLowercase);
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
