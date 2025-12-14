package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigScreen;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.PlaceholderWidget;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class WidgetManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier FANCY_TAB_HUD = SkyblockerMod.id("fancy_tab_hud");
	private static final Identifier FANCY_TAB = SkyblockerMod.id("fancy_tab");

	private static final int VERSION = 2;
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("hud_widgets.json");

	private static Config config = new Config(Map.of(), Map.of());

	private static final Map<Location, Map<ScreenLayer, ScreenBuilder>> BUILDER_MAP = new EnumMap<>(Location.class);

	public static final Object2ObjectMap<String, HudWidget> WIDGET_INSTANCES = new Object2ObjectOpenHashMap<>();

	public static HudWidget getWidgetOrPlaceholder(String id) {
		return WIDGET_INSTANCES.computeIfAbsent(id, PlaceholderWidget::new);
	}

	public static List<HudWidget> getWidgetsAvailableIn(Location location) {
		return WIDGET_INSTANCES.values().stream().filter(w -> w.getInformation().available().test(location)).toList();
	}

	public static ScreenBuilder getScreenBuilder(Location location, ScreenLayer layer) {
		return BUILDER_MAP
				.computeIfAbsent(location, l -> new EnumMap<>(ScreenLayer.class))
				.computeIfAbsent(layer, l -> new ScreenBuilder(config.perScreenConfig().getOrDefault(location, Map.of()).getOrDefault(l, new ScreenBuilder.ScreenConfig()), location == Location.UNKNOWN ? null : getScreenBuilder(Location.UNKNOWN, l)));
	}

	// we probably want this to run pretty early?
	@Init(priority = -1)
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {

			instantiateWidgets();
			for (int i = 1; i < 6; i++) {
				DungeonPlayerWidget widget = new DungeonPlayerWidget(i);
				addWidgetInstance(widget);
			}
			loadConfig();

		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());

		// Renders the hud (always on screen) widgets.
		HudElementRegistry.attachElementBefore(VanillaHudElements.DEMO_TIMER, FANCY_TAB_HUD, (context, tickCounter) -> render(context, true));
		// Renders the tab widgets
		HudElementRegistry.attachElementBefore(VanillaHudElements.PLAYER_LIST, FANCY_TAB, (context, tickCounter) -> render(context, false));

		PlayerListManager.registerTabListener(WidgetManager::onPlayerListChange);
	}

	private static void render(GuiGraphics context, boolean hud) {
		if (!Utils.isOnSkyblock()) return;
		Minecraft client = Minecraft.getInstance();

		if (client.screen instanceof WidgetsConfigScreen) return;
		float scale = TabHud.getScaleFactor();
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.scale(scale, scale);
		WidgetManager.render(context, TabHud.getHudWidth(), TabHud.getHudHeight(), hud);
		matrices.popMatrix();
	}

	private static ScreenBuilder currentBuilder = new ScreenBuilder(new ScreenBuilder.ScreenConfig(), null); // placeholder
	private static Location currentLocation;
	private static ScreenLayer currentLayer;

	/**
	 * Top level render method.
	 * Calls the appropriate ScreenBuilder with the screen's dimensions
	 *
	 * @param hud true to only render the hud (always on screen) widgets, false to only render the tab widgets.
	 */
	private static void render(GuiGraphics context, int w, int h, boolean hud) {
		Minecraft client = Minecraft.getInstance();
		Location location = Utils.getLocation();
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
		if (location != currentLocation || layer != currentLayer) {
			currentLocation = location;
			currentLayer = layer;
			currentBuilder = getScreenBuilder(currentLocation, currentLayer);
			currentBuilder.updateWidgetsList();
			currentBuilder.updateTabWidgetsList();
			currentBuilder.updatePositions(w, h);
		}
		currentBuilder.render(context, w, h, false);
	}

	/**
	 * @return true if the widget is enabled in the current screen.
	 */
	public static boolean isInCurrentScreen(HudWidget widget) {
		return currentBuilder.isInScreenBuilder(widget);
	}

	private static void onPlayerListChange() {
		currentBuilder.updateTabWidgetsList();
	}

	public static void loadConfig() {
		try (BufferedReader reader = Files.newBufferedReader(FILE)) {
			AtomicReference<String> error = new AtomicReference<>();
			config = Config.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader)).resultOrPartial(error::set).orElseThrow().getFirst();
			if (error.get() != null) { // separate it to not run when the config fully cannot load
				LOGGER.error("Failed to load part of the config", new Exception(error.get()));
				showErrorToast();
			}
			for (Object2ObjectMap.Entry<String, HudWidget> entry : WIDGET_INSTANCES.object2ObjectEntrySet()) {
				String key = entry.getKey();
				JsonObject jsonObject = config.widgetOptions.get(key);
				if (jsonObject == null) continue;
				HudWidget widget = entry.getValue();
				try {
					setWidgetOptions(widget, jsonObject);
				} catch (Exception e) {
					LOGGER.error("Failed to load config for {}", widget.getId(), e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load config", e);
			showErrorToast();
		}
	}

	private static void showErrorToast() {
		// TODO translatable
		SystemToast.add(Minecraft.getInstance().getToastManager(), new SystemToast.SystemToastId(), Component.literal("Error reading Skyblocker HUD Config"), Component.literal("Check your logs!"));
	}

	public static void saveConfig() {
		Map<String, JsonObject> widgetOptions = WIDGET_INSTANCES.values().stream().map(
				widget -> {
					List<WidgetOption<?>> options = new ArrayList<>();
					widget.getOptions(options);
					JsonObject object = new JsonObject();
					for (WidgetOption<?> option : options) {
						object.add(option.getId(), option.toJson());
					}
					return Pair.of(widget.getId(), object);
				}
		).collect(Collectors.toMap(Pair::first, Pair::second));
		// is it sign of bad code if intellij thinks I don't need to specify the generics when I actually do?
		//noinspection Convert2Diamond
		Map<Location, Map<ScreenLayer, ScreenBuilder.ScreenConfig>> perScreenConfig = new EnumMap<Location, Map<ScreenLayer, ScreenBuilder.ScreenConfig>>(BUILDER_MAP.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> new EnumMap<ScreenLayer, ScreenBuilder.ScreenConfig>(e.getValue().entrySet().stream().collect(Collectors.toMap(
						Map.Entry::getKey,
						f -> f.getValue().getConfig()
				))))));
		// merge with old Config since ScreenBuilders are only instantiated as needed.
		for (Map.Entry<Location, Map<ScreenLayer, ScreenBuilder.ScreenConfig>> entry : config.perScreenConfig().entrySet()) {
			perScreenConfig.merge(entry.getKey(), entry.getValue(), (currentMap, oldMap) -> {
				oldMap.forEach(currentMap::putIfAbsent);
				return currentMap;
			});
		}
		config.widgetOptions().forEach(widgetOptions::putIfAbsent);
		Config output = new Config(widgetOptions, perScreenConfig);
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			SkyblockerMod.GSON.toJson(Config.CODEC.encodeStart(JsonOps.INSTANCE, output).getOrThrow(), writer);
			LOGGER.info("[Skyblocker] Saved hud widget config");
		} catch (Exception e) {
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
		HudWidget previous = WIDGET_INSTANCES.put(widget.getId(), widget);
		if (previous != null && !(previous instanceof PlaceholderWidget)) LOGGER.warn("[Skyblocker] Duplicate hud widget found: {}", widget);
		JsonObject object = config.widgetOptions().get(widget.getId());
		if (object != null) {
			setWidgetOptions(widget, object);
		}
		if (Utils.isOnSkyblock()) {
			currentBuilder.updateWidgetsList();
			currentBuilder.updateTabWidgetsList();
		}
	}

	private static void setWidgetOptions(HudWidget widget, JsonObject object) {
		List<WidgetOption<?>> options = new ArrayList<>();
		widget.getOptions(options);
		for (WidgetOption<?> option : options) {
			JsonElement element = object.get(option.getId());
			if (element == null) continue;
			option.fromJson(element);
		}
	}

	private record Config(Map<String, JsonObject> widgetOptions, Map<Location, Map<ScreenLayer, ScreenBuilder.ScreenConfig>> perScreenConfig) {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, CodecUtils.JSON_OBJECT_CODEC).fieldOf("widget_options").forGetter(Config::widgetOptions),
				Codec.unboundedMap(Location.CODEC, Codec.unboundedMap(ScreenLayer.CODEC, ScreenBuilder.ScreenConfig.CODEC)).fieldOf("screens").forGetter(Config::perScreenConfig)
		).apply(instance, Config::new));
	}

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
}
