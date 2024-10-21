package de.hysky.skyblocker.skyblock.tabhud.config;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.preview.PreviewTab;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WidgetsConfigurationScreen extends Screen implements ScreenHandlerListener {
    public static final Logger LOGGER = LogUtils.getLogger();

    private GenericContainerScreenHandler handler;
    private String titleLowercase;
    public final boolean noHandler;
    private String widgetsLayer = null;
    private Screen parent = null;

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
    private Location currentLocation = Utils.getLocation();

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
    private WidgetsListTab widgetsListTab;

    private boolean switchingToPopup = false;

    /**
     * Creates the screen to configure, putting the handler at null will hide the first tab. Putting it to null is used in the config
     * @param handler the container handler
     * @param titleLowercase the title in lowercase
     */
    private WidgetsConfigurationScreen(@Nullable GenericContainerScreenHandler handler, String titleLowercase, Location targetLocation, @Nullable String widgetLayerToGoTo) {
        super(Text.literal("Widgets Configuration"));
        this.handler = handler;
        this.titleLowercase = titleLowercase;
        this.noHandler = handler == null;
        if (!noHandler) {
            this.handler.addListener(this);
            parseLocation();
        } else {
            currentLocation = targetLocation;
            widgetsLayer = widgetLayerToGoTo;
        }
        ScreenMaster.getScreenBuilder(currentLocation).backupPositioning();
    }

    /**
     * Create the screen for when backed by hypixel's widgets menu
     * @param handler the container handler
     * @param titleLowercase the title in lowercase, to figure out where you are
     */
    public WidgetsConfigurationScreen(@NotNull GenericContainerScreenHandler handler, String titleLowercase) {
        this(handler, titleLowercase, Location.UNKNOWN, null);
    }

    /**
     * Create the screen specifically for the config screen, the widgets tab will be unavailable
     * @param targetLocation open the preview to this location
     * @param widgetLayerToGoTo go to this widget's layer
     */
    public WidgetsConfigurationScreen(Location targetLocation, String widgetLayerToGoTo, Screen parent) {
        this(null, "", targetLocation, widgetLayerToGoTo);
        this.parent = parent;
    }

    @Override
    protected void init() {
        previewTab = new PreviewTab(this.client, this, noHandler ? PreviewTab.Mode.EDITABLE_LOCATION : PreviewTab.Mode.NORMAL);
        PreviewTab previewDungeons = new PreviewTab(this.client, this, PreviewTab.Mode.DUNGEON);
        if (noHandler)  {
            this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width)
                    .tabs(this.previewTab, previewDungeons)
                    .build();
            previewTab.goToLayer(ScreenMaster.getScreenBuilder(currentLocation).getPositionRuleOrDefault(widgetsLayer).screenLayer());
        } else {
            widgetsListTab = new WidgetsListTab(this.client, this.handler);
            this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width)
                    .tabs(this.widgetsListTab, this.previewTab, previewDungeons)
                    .build();
			widgetsListTab.setShouldShowEntries(titleLowercase.startsWith("widgets "));
			updateCustomWidgets();
        }
        this.tabNavigation.selectTab(0, false);
        switchingToPopup = false;
        this.addDrawableChild(tabNavigation);
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        if (this.tabNavigation != null) {
            this.tabNavigation.setWidth(this.width);
            this.tabNavigation.init();
            int i = this.tabNavigation.getNavigationFocus().getBottom();
            ScreenRect screenRect = new ScreenRect(0, i, this.width, this.height - i - 5);
            this.tabManager.setTabArea(screenRect);
        }
    }

    public void updateHandler(GenericContainerScreenHandler newHandler, String titleLowercase) {
        if (noHandler) return;
        handler.removeListener(this);
        handler = newHandler;
        handler.addListener(this);
        this.titleLowercase = titleLowercase;
        parseLocation();
        widgetsListTab.updateHandler(handler);
    }

	public void updateCustomWidgets() {
		List<WidgetEntry> entries = new ArrayList<>();
		for (HudWidget value : ScreenMaster.widgetInstances.values()) {
			if (!value.availableLocations().contains(currentLocation)) continue;
			entries.add(new WidgetEntry(value, currentLocation));
		}
		widgetsListTab.setEntries(entries);
	}

	public void setCurrentLocation(Location location) {
		Location old = this.currentLocation;
		currentLocation = location;
		if (old != currentLocation) {
			ScreenMaster.getScreenBuilder(currentLocation).backupPositioning();
			updateCustomWidgets();
		}
	}

    private void parseLocation() {
		boolean b = titleLowercase.startsWith("widgets ");
		if (widgetsListTab != null) widgetsListTab.setShouldShowEntries(b);
		String trim = this.titleLowercase
                .replace("widgets in", "")
                .replace("widgets on", "")
                .trim();

        if (nameToLocation.containsKey(trim)) {
            setCurrentLocation(nameToLocation.get(trim));
        } else {
            //currentLocation = Utils.getLocation();
            if (b)
                LOGGER.warn("[Skyblocker] Couldn't find location for {} (trimmed: {})", this.titleLowercase, trim);
        }
    }

    public GenericContainerScreenHandler getHandler() {
        return handler;
    }

    private @Nullable ItemStack slotThirteenBacklog = null;

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        if (noHandler) return;
        if (slotId == 4) {
            tabPreview = stack.isOf(Items.PLAYER_HEAD);
        }
        if (widgetsListTab == null) {
            if (slotId == 13) slotThirteenBacklog = stack.copy();
            return;
        }
        if (slotId == 13) {
            if (stack.isOf(Items.HOPPER)) {
                widgetsListTab.hopper(ItemUtils.getLore(stack));
            } else {
                widgetsListTab.hopper(null);
            }
        }
        if (slotId > (titleLowercase.startsWith("tablist widgets") ? 9 : 18) && slotId < this.handler.getRows() * 9 - 9 || slotId == 45 || slotId == 53 || slotId == 50) {
            widgetsListTab.onSlotChange(slotId, stack);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (noHandler) return;
        if (slotThirteenBacklog != null && widgetsListTab != null) {
            widgetsListTab.hopper(ItemUtils.getLore(slotThirteenBacklog));
            slotThirteenBacklog = null;
        }
        if (!this.client.player.isAlive() || this.client.player.isRemoved()) {
            this.client.player.closeHandledScreen();
        }
    }

    @Override
    public void close() {
        if (!noHandler) {
            this.client.player.closeHandledScreen();
            super.close();
        } else {
            client.setScreen(parent);
        }
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

    @Override
    public void removed() {
        if (noHandler) return;
        if (!switchingToPopup && this.client != null && this.client.player != null) {
            this.handler.onClosed(this.client.player);
        }
        handler.removeListener(this);
        Scheduler.INSTANCE.schedule(PlayerListMgr::updateList, 1);
		SkyblockerConfigManager.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public DropdownWidget<Location> createLocationDropdown(Consumer<Location> onLocationChanged) {
        List<Location> locations = new ArrayList<>(List.of(Location.hudLocations()));
        locations.remove(Location.DUNGEON); // there's already a tab for that
        return new DropdownWidget<>(client, 0, 0, 50, 50, locations, location -> {
            setCurrentLocation(location);
            onLocationChanged.accept(location);
        }, locations.contains(currentLocation) ? currentLocation : Location.HUB);
    }
}
