package de.hysky.skyblocker.skyblock.tabhud.config;

import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.preview.PreviewTab;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WidgetsConfigurationScreen extends Screen implements ContainerListener {
	public static final Logger LOGGER = LogUtils.getLogger();

	private @Nullable ChestMenu handler;
	private String titleLowercase;
	public final boolean noHandler;
	private WidgetManager.ScreenLayer widgetsLayer = null;
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
			Map.entry("the mineshaft", Location.GLACITE_MINESHAFTS),
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
	private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
	private TabNavigationBar tabNavigation;
	private WidgetsListTab widgetsListTab;

	private boolean switchingToPopup = false;

	/**
	 * Register the /skyblocker hud command, which will open /widgets if on Skyblock and Fancy Tab Hud is enabled.
	 * Otherwise, it'll open the widgets config screen.
	 */
	@Init
	public static void initCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("hud").executes((ctx) -> {
				if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled) {
					MessageScheduler.INSTANCE.sendMessageAfterCooldown("/widgets", true);
				} else {
					Location currentLocation = Utils.isOnSkyblock() ? Utils.getLocation() : Location.HUB;
					MessageScheduler.queueOpenScreen(new WidgetsConfigurationScreen(currentLocation, WidgetManager.ScreenLayer.MAIN_TAB, null));
				}
				return Command.SINGLE_SUCCESS;
			})));
		});
	}

	/**
	 * Creates the screen to configure, putting the handler at null will hide the first tab. Putting it to null is used in the config
	 *
	 * @param handler        the container handler
	 * @param titleLowercase the title in lowercase
	 */
	private WidgetsConfigurationScreen(@Nullable ChestMenu handler, String titleLowercase, Location targetLocation, WidgetManager.@Nullable ScreenLayer widgetLayerToGoTo) {
		super(Component.literal("Widgets Configuration"));
		this.handler = handler;
		this.titleLowercase = titleLowercase;
		this.noHandler = handler == null;
		if (!noHandler) {
			this.handler.addSlotListener(this);
			parseLocation();
		} else {
			currentLocation = targetLocation;
			widgetsLayer = widgetLayerToGoTo;
		}
		WidgetManager.getScreenBuilder(currentLocation).backupPositioning();
	}

	/**
	 * Create the screen for when backed by hypixel's widgets menu
	 *
	 * @param handler        the container handler
	 * @param titleLowercase the title in lowercase, to figure out where you are
	 */
	public WidgetsConfigurationScreen(ChestMenu handler, String titleLowercase) {
		this(handler, titleLowercase, Location.UNKNOWN, null);
	}

	/**
	 * Create the screen specifically for the config screen, the widgets tab will be unavailable
	 *
	 * @param targetLocation    open the preview to this location
	 * @param widgetLayerToGoTo go to this widget's layer
	 */
	public WidgetsConfigurationScreen(Location targetLocation, String widgetLayerToGoTo, Screen parent) {
		this(null, "", targetLocation, WidgetManager.getScreenBuilder(targetLocation).getPositionRuleOrDefault(widgetLayerToGoTo).screenLayer());
		this.parent = parent;
	}

	/**
	 * Create the screen specifically for the config screen, the widgets tab will be unavailable
	 *
	 * @param targetLocation open the preview to this location
	 * @param layerToGo      go to this layer
	 */
	public WidgetsConfigurationScreen(Location targetLocation, WidgetManager.ScreenLayer layerToGo, Screen parent) {
		this(null, "", targetLocation, layerToGo);
		this.parent = parent;
	}

	@Override
	protected void init() {
		previewTab = new PreviewTab(this.minecraft, this, noHandler ? PreviewTab.Mode.EDITABLE_LOCATION : PreviewTab.Mode.NORMAL);
		PreviewTab previewDungeons = new PreviewTab(this.minecraft, this, PreviewTab.Mode.DUNGEON);
		if (noHandler) {
			previewTab.goToLayer(widgetsLayer);
		}
		widgetsListTab = new WidgetsListTab(this.minecraft, this.handler);
		this.tabNavigation = TabNavigationBar.builder(this.tabManager, this.width)
				.addTabs(this.widgetsListTab, this.previewTab, previewDungeons)
				.build();
		widgetsListTab.setShouldShowCustomWidgetEntries(titleLowercase.startsWith("widgets ") || noHandler);
		updateCustomWidgets();

		this.tabNavigation.selectTab(0, false);
		switchingToPopup = false;
		this.addRenderableWidget(tabNavigation);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		if (this.tabNavigation != null) {
			this.tabNavigation.setWidth(this.width);
			this.tabNavigation.arrangeElements();
			int i = this.tabNavigation.getRectangle().bottom();
			ScreenRectangle screenRect = new ScreenRectangle(0, i, this.width, this.height - i - 5);
			this.tabManager.setTabArea(screenRect);
		}
	}

	public void updateHandler(ChestMenu newHandler, String titleLowercase) {
		if (handler == null) return;
		handler.removeSlotListener(this);
		handler = newHandler;
		handler.addSlotListener(this);
		this.titleLowercase = titleLowercase;
		parseLocation();
		widgetsListTab.updateHandler(handler);
	}

	public void updateCustomWidgets() {
		List<WidgetEntry> entries = new ArrayList<>();
		for (HudWidget value : WidgetManager.widgetInstances.values()) {
			if (!value.availableLocations().contains(currentLocation)) continue;
			entries.add(new WidgetEntry(value, currentLocation));
		}
		widgetsListTab.setCustomWidgetEntries(entries);
	}

	public void setCurrentLocation(Location location) {
		Location old = this.currentLocation;
		currentLocation = location;
		if (old != currentLocation) {
			WidgetManager.getScreenBuilder(currentLocation).backupPositioning();
			updateCustomWidgets();
		}
	}

	private void parseLocation() {
		boolean b = titleLowercase.startsWith("widgets ");
		if (widgetsListTab != null) widgetsListTab.setShouldShowCustomWidgetEntries(b);
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

	public @Nullable ChestMenu getHandler() {
		return handler;
	}

	private @Nullable ItemStack slotThirteenBacklog = null;

	@Override
	public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
		if (this.handler == null) return;
		if (slotId == 4) {
			tabPreview = stack.is(Items.PLAYER_HEAD);
		}
		if (widgetsListTab == null) {
			if (slotId == 13) slotThirteenBacklog = stack.copy();
			return;
		}
		if (slotId == 13) {
			if (stack.is(Items.HOPPER)) {
				widgetsListTab.hopper(stack.skyblocker$getLoreStrings());
			} else {
				widgetsListTab.hopper(null);
			}
		}
		if (slotId > (titleLowercase.startsWith("tablist widgets") ? 9 : 18) && slotId < this.handler.getRowCount() * 9 - 9 || slotId == 45 || slotId == 53 || slotId == 50) {
			widgetsListTab.onSlotChange(slotId, stack);
		}
	}

	private void getBackOnTheScreenYouScallywagsAngryEmoji() {
		if (isDragging() || !(tabManager.getCurrentTab() instanceof PreviewTab tab)) return;
		ScreenBuilder builder = WidgetManager.getScreenBuilder(tab.getCurrentLocation());
		List<HudWidget> widgets = builder.getHudWidgets(tab.getCurrentScreenLayer());
		boolean needReposition = false;
		float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
		int padding = 2;
		ScreenRectangle screenRect = new ScreenRectangle(padding, padding, (int) (width / scale) - padding * 2, (int) (height / scale) - padding * 2);
		for (HudWidget widget : widgets) {
			PositionRule rule = builder.getPositionRule(widget.getInternalID());
			if (rule != null && !widget.getRectangle().intersects(screenRect)) {
				needReposition = true;
				builder.setPositionRule(widget.getInternalID(), new PositionRule(
						"screen",
						PositionRule.Point.DEFAULT,
						PositionRule.Point.DEFAULT,
						5,
						5,
						rule.screenLayer()
				));
			}
		}
		if (needReposition) tab.updateWidgets();
	}

	@Override
	public void tick() {
		super.tick();
		getBackOnTheScreenYouScallywagsAngryEmoji();
		if (noHandler) return;
		if (slotThirteenBacklog != null && widgetsListTab != null) {
			widgetsListTab.hopper(slotThirteenBacklog.skyblocker$getLoreStrings());
			slotThirteenBacklog = null;
		}
		assert this.minecraft != null;
		assert this.minecraft.player != null;
		if (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved()) {
			this.minecraft.player.closeContainer();
		}
	}

	@Override
	public void onClose() {
		assert this.minecraft != null;
		if (handler != null) {
			assert this.minecraft.player != null;
			this.minecraft.player.closeContainer();
			super.onClose();
		} else {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu handler, int property, int value) {}

	@Override
	public void removed() {
		if (handler == null) return;
		if (!switchingToPopup && this.minecraft != null && this.minecraft.player != null) {
			this.handler.removed(this.minecraft.player);
		}
		handler.removeSlotListener(this);
		Scheduler.INSTANCE.schedule(PlayerListManager::updateList, 1);
		SkyblockerConfigManager.save();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public DropdownWidget<Location> createLocationDropdown(Consumer<Location> onLocationChanged) {
		List<Location> locations = Arrays.asList(ArrayUtils.removeElements(Location.values(), Location.UNKNOWN, Location.DUNGEON));  // there's already a tab for dungeons
		return new DropdownWidget<>(minecraft, 0, 0, 50, 50, locations, location -> {
			setCurrentLocation(location);
			onLocationChanged.accept(location);
		},
				locations.contains(currentLocation) ? currentLocation : Location.HUB,
				(isOpen) -> previewTab.locationDropdownOpened(isOpen));
	}
}
