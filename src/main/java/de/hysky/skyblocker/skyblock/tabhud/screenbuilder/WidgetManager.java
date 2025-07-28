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
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigScreen;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
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
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class WidgetManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier FANCY_TAB_HUD = Identifier.of(SkyblockerMod.NAMESPACE, "fancy_tab_hud");
	private static final Identifier FANCY_TAB = Identifier.of(SkyblockerMod.NAMESPACE, "fancy_tab");

	private static final int VERSION = 2;
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("hud_widgets.json");

	private static Config config = new Config(Map.of(), Map.of());

	private static final Map<Location, Map<ScreenLayer, ScreenBuilder>> BUILDER_MAP = new EnumMap<>(Location.class);

	public static final Object2ObjectMap<String, HudWidget> WIDGET_INSTANCES = new Object2ObjectOpenHashMap<>();

	public static @NotNull HudWidget getWidgetOrPlaceholder(String id) {
		return WIDGET_INSTANCES.computeIfAbsent(id, PlaceholderWidget::new);
	}

	public static List<HudWidget> getWidgetsAvailableIn(Location location) {
		return WIDGET_INSTANCES.values().stream().filter(w -> w.getInformation().available().test(location)).toList();
	}

	public static ScreenBuilder getScreenBuilder(Location location, ScreenLayer layer) {
		return BUILDER_MAP
				.computeIfAbsent(location, l -> new EnumMap<>(ScreenLayer.class))
				.computeIfAbsent(layer, l -> new ScreenBuilder(config.perScreenConfig().getOrDefault(location, Map.of()).getOrDefault(l, new JsonObject()).deepCopy(), location == Location.UNKNOWN ? null : getScreenBuilder(Location.UNKNOWN, l)));
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

		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer
				// Renders the hud (always on screen) widgets.
				// Since each layer has a z offset of 200 automatically added, attaching fancy tab hud before the demo timer is just enough for items to render below the debug hud
				.attachLayerBefore(IdentifiedLayer.DEMO_TIMER, FANCY_TAB_HUD, (context, tickCounter) -> render(context, true))
				// Renders the tab widgets
				.attachLayerBefore(IdentifiedLayer.PLAYER_LIST, FANCY_TAB, (context, tickCounter) -> render(context, false))
		);
	}

	private static void render(DrawContext context, boolean hud) {
		if (!Utils.isOnSkyblock()) return;
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.currentScreen instanceof WidgetsConfigScreen) return;
		Window window = client.getWindow();
		float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f;
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(scale, scale, 1.F);
		WidgetManager.render(context, (int) (window.getScaledWidth() / scale), (int) (window.getScaledHeight() / scale), hud);
		matrices.pop();
	}

	private static ScreenBuilder currentBuilder = getScreenBuilder(Location.UNKNOWN, ScreenLayer.HUD);
	private static Location currentLocation = Location.UNKNOWN;
	private static ScreenLayer currentLayer = ScreenLayer.HUD;

	/**
	 * Top level render method.
	 * Calls the appropriate ScreenBuilder with the screen's dimensions
	 *
	 * @param hud true to only render the hud (always on screen) widgets, false to only render the tab widgets.
	 */
	private static void render(DrawContext context, int w, int h, boolean hud) {
		MinecraftClient client = MinecraftClient.getInstance();
		Location location = Utils.getLocation();
		ScreenLayer layer;
		if (client.options.playerListKey.isPressed()) {
			if (hud || TabHud.shouldRenderVanilla()) return;
			if (TabHud.toggleSecondary.isPressed()) {
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
		}
		currentBuilder.render(context, w, h, false);
	}

	public static void loadConfig() {
		try (BufferedReader reader = Files.newBufferedReader(FILE)) {
			config = Config.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow().getFirst();
			for (Object2ObjectMap.Entry<String, HudWidget> entry : WIDGET_INSTANCES.object2ObjectEntrySet()) {
				String key = entry.getKey();
				JsonObject jsonObject = config.widgetOptions.get(key);
				if (jsonObject == null) continue;
				HudWidget widget = entry.getValue();
				setWidgetOptions(widget, jsonObject);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load config", e);
			// TODO translatable
			SystemToast.add(MinecraftClient.getInstance().getToastManager(), new SystemToast.Type(), Text.literal("Error reading Skyblocker HUD Config"), Text.literal("Check your logs!"));
		}
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
					return Pair.of(widget.getInformation().id(), object);
				}
		).collect(Collectors.toMap(Pair::first, Pair::second));
		Map<Location, Map<ScreenLayer, JsonObject>> perScreenConfig = BUILDER_MAP.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> e.getValue().entrySet().stream().collect(Collectors.toMap(
						Map.Entry::getKey,
						f -> f.getValue().getConfig()
				))));
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
		HudWidget previous = WIDGET_INSTANCES.put(widget.getInformation().id(), widget);
		if (previous != null && !(previous instanceof PlaceholderWidget)) LOGGER.warn("[Skyblocker] Duplicate hud widget found: {}", widget);
		JsonObject object = config.widgetOptions().get(widget.getInformation().id());
		if (object != null) {
			setWidgetOptions(widget, object);
		}
	}

	public static void setWidgetOptions(HudWidget widget, JsonObject object) {
		List<WidgetOption<?>> options = new ArrayList<>();
		widget.getOptions(options);
		for (WidgetOption<?> option : options) {
			JsonElement element = object.get(option.getId());
			if (element == null) continue;
			option.fromJson(element);
		}
	}

	private record Config(Map<String, JsonObject> widgetOptions, Map<Location, Map<ScreenLayer, JsonObject>> perScreenConfig) {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, CodecUtils.JSON_OBJECT_CODEC).fieldOf("widget_options").forGetter(Config::widgetOptions),
				Codec.unboundedMap(Location.CODEC, Codec.unboundedMap(ScreenLayer.CODEC, CodecUtils.JSON_OBJECT_CODEC)).fieldOf("screens").forGetter(Config::perScreenConfig)
		).apply(instance, Config::new));
	}

	public enum ScreenLayer implements StringIdentifiable {
		MAIN_TAB,
		SECONDARY_TAB,
		HUD;

		public static final Codec<ScreenLayer> CODEC = StringIdentifiable.createCodec(ScreenLayer::values);

		@Override
		public String toString() {
			return switch (this) {
				case MAIN_TAB -> "Main Tab";
				case SECONDARY_TAB -> "Secondary Tab";
				case HUD -> "HUD";
			};
		}

		@Override
		public String asString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
