package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.galatea.SweepDetailsHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.config.ConfigLayerBuilder;
import de.hysky.skyblocker.skyblock.tabhud.config.ConfigScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.PlaceholderWidget;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class WidgetManager {
	@SuppressWarnings("deprecation")
	public static final Set<Location> ALLOWED_LOCATIONS = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(Location.UNKNOWN, Location.BLAZING_FORTRESS)));
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier FANCY_TAB_HUD = SkyblockerMod.id("fancy_tab_hud");
	private static final Identifier FANCY_TAB = SkyblockerMod.id("fancy_tab");

	private static final int VERSION = 2;
	private static final String VERSION_KEY = "_version";
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("hud_widgets.json");

	public static final ScreenBuilder SCREEN_BUILDER = new ScreenBuilder();

	private static Config CONFIG = new Config();

	public static final Map<String, HudWidget> WIDGET_INSTANCES = new HashMap<>();

	private static boolean showOldVersionMessage = false;
	private static boolean hasFancyTab = false;

	public static ScreenConfig getScreenConfig(Location screenId) {
		return CONFIG.screenConfigs().computeIfAbsent(screenId, _ -> new ScreenConfig());
	}

	public static CopyTracker getCopyTracker() {
		return CONFIG.copyTracker();
	}

	public static boolean hasFancyTab() {
		return hasFancyTab;
	}

	public static HudWidget getWidgetOrPlaceholder(String id) {
		return WIDGET_INSTANCES.computeIfAbsent(id, PlaceholderWidget::new);
	}

	public static boolean isWidgetInCurrentScreen(HudWidget widget) {
		return SCREEN_BUILDER.contains(widget);
	}

	public static boolean isWidgetInCurrentLayer(HudWidget widget) {
		return currentLayer != null && SCREEN_BUILDER.get(currentLayer).contains(widget);
	}

	public static List<HudWidget> getWidgetsAvailableIn(Location location) {
		return WIDGET_INSTANCES.values().stream().filter(w -> w.getInformation().available().test(location)).toList();
	}

	private static @Nullable Location currentLocation;
	private static @Nullable ScreenLayer currentLayer;

	// we probably want this to run pretty early?
	@Init(priority = -1)
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_ -> {

			instantiateWidgets();
			for (int i = 1; i < 6; i++) {
				DungeonPlayerWidget widget = new DungeonPlayerWidget(i);
				addWidgetInstance(widget);
			}
			loadConfig();

			PlayerListManager.registerTabListener(WidgetManager::onPlayerListUpdate);
		});

		SkyblockEvents.JOIN.register(() -> {
			if (showOldVersionMessage) {
				Scheduler.INSTANCE.schedule(() -> {
					if (Minecraft.getInstance().player == null) return;
					Minecraft.getInstance().player.sendSystemMessage(Constants.PREFIX.get().append(
							"The HUD system has changed! Sadly your previous config couldn't be ported over due to complications... Check out /skyblocker hud!"
					));
					showOldVersionMessage = false;
				}, 10 * 20); // displays a bit too early and gets burried by tips and other stuff instantly, so we delay a bit
			}
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> saveConfig());

		// Renders the hud (always on screen) widgets.
		HudElementRegistry.attachElementBefore(VanillaHudElements.DEMO_TIMER, FANCY_TAB_HUD, (context, _) -> extractRenderState(context, true));
		// Renders the tab widgets
		HudElementRegistry.attachElementBefore(VanillaHudElements.PLAYER_LIST, FANCY_TAB, (context, _) -> extractRenderState(context, false));
	}

	private static void extractRenderState(GuiGraphicsExtractor context, boolean hud) {
		if (!Utils.isOnSkyblock()) return;
		Minecraft client = Minecraft.getInstance();

		if (client.screen instanceof WidgetsConfigurationScreen) return;
		Window window = client.getWindow();
		float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f;
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.scale(scale, scale);
		WidgetManager.extractRenderState(context, (int) (window.getGuiScaledWidth() / scale), (int) (window.getGuiScaledHeight() / scale), hud);
		matrices.popMatrix();
	}

	public static void onPlayerListUpdate() {
		boolean fancyTab = SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableFancyTab;
		if (!fancyTab && hasFancyTab) {
			SCREEN_BUILDER.clearFancyTab();
		} else if (fancyTab) {
			SCREEN_BUILDER.updateFancyTab();
			hasFancyTab = true;
		}
	}

	/**
	 * Top level render method.
	 * Calls the appropriate LayerBuilder with the screen's dimensions
	 *
	 * @param hud true to only render the hud (always on screen) widgets, false to only render the tab widgets.
	 */
	private static void extractRenderState(GuiGraphicsExtractor context, int w, int h, boolean hud) {
		Minecraft client = Minecraft.getInstance();
		ScreenLayer layer;
		if (client.options.keyPlayerList.isDown()) {
			if (hud || TabHud.shouldRenderVanilla()) return;
			if (TabHud.toggleSecondary.isDown()) {
				layer = ScreenLayer.SECONDARY_TAB;
			} else {
				layer = ScreenLayer.MAIN_TAB;
			}
		} else if (hud) {
			layer = ScreenLayer.HUD;
		} else return;
		Location location = Utils.getLocation();
		if (currentLocation != location) {
			currentLocation = location;
			SCREEN_BUILDER.setConfig(getScreenConfig(currentLocation));
			currentLayer = null; // force second condition to trigger
		}
		if (currentLayer != layer) {
			currentLayer = layer;
			SCREEN_BUILDER.get(layer).update();
		}
		SCREEN_BUILDER.get(currentLayer).extractRenderStates(context, w, h, false);
	}

	public static void loadConfig() {
		try (BufferedReader reader = Files.newBufferedReader(FILE)) {
			AtomicReference<@Nullable String> error = new AtomicReference<>();
			JsonElement input = JsonParser.parseReader(reader);
			if (!(input instanceof JsonObject object) || !object.has(VERSION_KEY)) {
				// Don't bother TRYING to load if it's the old format.
				showOldVersionMessage = true;
				fillDefaultConfig(0);
				LOGGER.info("[Skyblocker] Old HUD config detected. Ignoring :(");
				return;
			}
			if (object.get(VERSION_KEY).isJsonPrimitive()) fillDefaultConfig(object.get(VERSION_KEY).getAsInt());
			CONFIG = Config.CODEC.decode(JsonOps.INSTANCE, input).resultOrPartial(error::set).orElseThrow().getFirst();
			if (error.get() != null) { // separate it to not run when the config fully cannot load
				LOGGER.error("[Skyblocker] Failed to load part of the HUD config", new Exception(error.get()));
				showErrorToast();
			}
		} catch (NoSuchFileException _) {
			// Fill default config
			fillDefaultConfig(0);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to HUD load config", e);
			showErrorToast();
		}
	}

	private static void showErrorToast() {
		SystemToast.add(Minecraft.getInstance().getToastManager(), new SystemToast.SystemToastId(), Component.literal("Error reading Skyblocker HUD Config"), Component.literal("Check your logs!"));
	}

	private static void fillDefaultConfig(int comingFromVersion) {
		if (comingFromVersion <= 0) {
			ConfigScreenBuilder configScreenBuilder = new ConfigScreenBuilder();
			ConfigLayerBuilder hud = configScreenBuilder.hud();
			// Mining related stuff

			HudWidget commissions = getWidgetOrPlaceholder("commissions");
			HudWidget powders = getWidgetOrPlaceholder("powders");
			EnumSet<Location> miningLocations = EnumSet.of(Location.CRYSTAL_HOLLOWS, Location.DWARVEN_MINES, Location.GLACITE_MINESHAFTS);
			getCopyTracker().hud().getOrCreate(commissions.getInternalID()).track(miningLocations);
			getCopyTracker().hud().getOrCreate(powders.getInternalID()).track(miningLocations);

			PositionRule commsRule = new PositionRule(
					Optional.empty(),
					new PositionRule.Point(PositionRule.VerticalPoint.BOTTOM, PositionRule.HorizontalPoint.LEFT),
					new PositionRule.Point(PositionRule.VerticalPoint.TOP, PositionRule.HorizontalPoint.LEFT),
					5,
					5
			);
			PositionRule powderRule = new PositionRule(
					"commissions",
					new PositionRule.Point(PositionRule.VerticalPoint.BOTTOM, PositionRule.HorizontalPoint.LEFT),
					new PositionRule.Point(PositionRule.VerticalPoint.TOP, PositionRule.HorizontalPoint.LEFT),
					0,
					2
			);
			configScreenBuilder.setConfig(getScreenConfig(Location.DWARVEN_MINES));
			hud.add(commissions, commsRule);
			hud.add(powders, powderRule);
			hud.serializeConfig();


			configScreenBuilder.setConfig(getScreenConfig(Location.CRYSTAL_HOLLOWS));
			hud.add(commissions, commsRule);
			hud.add(powders, powderRule);
			hud.add(getWidgetOrPlaceholder("hud_crystals"), new PositionRule(
					Optional.empty(),
					new PositionRule.Point(PositionRule.VerticalPoint.TOP, PositionRule.HorizontalPoint.RIGHT),
					new PositionRule.Point(PositionRule.VerticalPoint.TOP, PositionRule.HorizontalPoint.RIGHT),
					-5,
					-5
			));
			hud.serializeConfig();

			configScreenBuilder.setConfig(getScreenConfig(Location.GLACITE_MINESHAFTS));
			hud.add(commissions, commsRule);
			hud.add(powders, powderRule);
			hud.serializeConfig();

			// Sweep details
			HudWidget sweepDetails = getWidgetOrPlaceholder("sweep_details");
			for (Location location : SweepDetailsHudWidget.LOCATIONS) {
				configScreenBuilder.setConfig(getScreenConfig(location));
				hud.add(sweepDetails);
				hud.serializeConfig();
			}
			getCopyTracker().hud().getOrCreate(sweepDetails.getInternalID()).track(SweepDetailsHudWidget.LOCATIONS);

			// Galatea
			configScreenBuilder.setConfig(getScreenConfig(Location.GALATEA));
			hud.add(getWidgetOrPlaceholder("hud_treeprogress"), new PositionRule(
					"sweep_details",
					new PositionRule.Point(PositionRule.VerticalPoint.BOTTOM, PositionRule.HorizontalPoint.LEFT),
					new PositionRule.Point(PositionRule.VerticalPoint.TOP, PositionRule.HorizontalPoint.LEFT),
					0,
					2
			));
			hud.serializeConfig();

			// Garden
			configScreenBuilder.setConfig(getScreenConfig(Location.GARDEN));
			hud.add(getWidgetOrPlaceholder("hud_farming"));
			hud.serializeConfig();

			// The end
			configScreenBuilder.setConfig(getScreenConfig(Location.THE_END));
			hud.add(getWidgetOrPlaceholder("hud_end"));
			hud.serializeConfig();

			configScreenBuilder.setConfig(getScreenConfig(Location.DUNGEON));
			hud.add(getWidgetOrPlaceholder("dungeon_splits"), new PositionRule(
					Optional.empty(),
					new PositionRule.Point(PositionRule.VerticalPoint.CENTER, PositionRule.HorizontalPoint.LEFT),
					new PositionRule.Point(PositionRule.VerticalPoint.CENTER, PositionRule.HorizontalPoint.LEFT),
					5,
					0)
			);
			hud.serializeConfig();
		}
	}

	public static void saveConfig() {
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			JsonElement element = Config.CODEC.encodeStart(JsonOps.INSTANCE, CONFIG).getOrThrow();
			element.getAsJsonObject().addProperty(VERSION_KEY, VERSION);
			SkyblockerMod.GSON.toJson(element, writer);
			LOGGER.info("[Skyblocker] Saved hud widget config");
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to save hud widget config", e);
		}
	}


	/**
	 * Filled at compile item with ASM.
	 * Do not fill this method or change the signature unless you know what you're doing
	 */
	private static void instantiateWidgets() {}

	/**
	 * Called by the ASM generated code to add a widget instance to the map.
	 * Do not change the signature unless you know what you're doing.
	 */
	public static void addWidgetInstance(HudWidget widget) {
		HudWidget put = WIDGET_INSTANCES.put(widget.getInternalID(), widget);
		if (put != null && !(put instanceof PlaceholderWidget)) LOGGER.warn("[Skyblocker] Duplicate hud widget found: {}", widget);
	}

	/**
	 * @implNote !! The 3 first ones shouldn't be moved, ordinal is used in some places
	 */
	public enum ScreenLayer implements StringRepresentable {
		MAIN_TAB,
		SECONDARY_TAB,
		HUD;

		public static final Codec<ScreenLayer> CODEC = StringRepresentable.fromEnum(ScreenLayer::values);

		@Override
		public String toString() {
			return switch (this) {
				case MAIN_TAB -> "Main Tab";
				case SECONDARY_TAB -> "Secondary Tab";
				case HUD -> "HUD";
			};
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	public record Config(Map<Location, ScreenConfig> screenConfigs, CopyTracker copyTracker) {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CodecUtils.mutableOptional(Codec.unboundedMap(Location.CODEC, ScreenConfig.CODEC).fieldOf("widgets"), Object2ObjectOpenHashMap::new).forGetter(Config::screenConfigs),
				CopyTracker.CODEC.fieldOf("copies").forGetter(Config::copyTracker)
		).apply(instance, Config::new));

		public Config() {
			this(new Object2ObjectOpenHashMap<>(), new CopyTracker());
		}
	}
}
