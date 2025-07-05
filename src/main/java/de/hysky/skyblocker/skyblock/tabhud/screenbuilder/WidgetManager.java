package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.PlaceholderWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.*;

public class WidgetManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier FANCY_TAB_HUD = Identifier.of(SkyblockerMod.NAMESPACE, "fancy_tab_hud");
	private static final Identifier FANCY_TAB = Identifier.of(SkyblockerMod.NAMESPACE, "fancy_tab");

	private static final int VERSION = 2;
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("hud_widgets.json");

	private static final Map<Location, Map<ScreenLayer, ScreenBuilder>> BUILDER_MAP = new EnumMap<>(Location.class);

	public static final Map<String, HudWidget> WIDGET_INSTANCES = new HashMap<>();

	public static @NotNull HudWidget getWidgetOrPlaceholder(String id) {
		return WIDGET_INSTANCES.computeIfAbsent(id, PlaceholderWidget::new);
	}

	public static List<HudWidget> getWidgetsAvailableIn(Location location) {
		return WIDGET_INSTANCES.values().stream().filter(w -> w.getInformation().available().test(location)).toList();
	}

	public static ScreenBuilder getScreenBuilder(Location location, ScreenLayer layer) {
		return BUILDER_MAP
				.computeIfAbsent(location, l -> new EnumMap<>(ScreenLayer.class))
				.computeIfAbsent(layer, l -> new ScreenBuilder(new JsonObject(), BUILDER_MAP.get(Location.UNKNOWN).get(l)));
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
			System.out.println(location + " " + layer);
			currentLocation = location;
			currentLayer = layer;
			currentBuilder = getScreenBuilder(currentLocation, currentLayer);
			currentBuilder.updateWidgetsList();
		}
		currentBuilder.render(context, w, h, false);
	}

	public static void loadConfig() {
		/*try (BufferedReader reader = Files.newBufferedReader(FILE)) {
			JsonObject object = SkyblockerMod.GSON.fromJson(reader, JsonObject.class);
			JsonObject positions = object.getAsJsonObject("positions");
			for (Map.Entry<Location, ScreenBuilder> builderEntry : BUILDER_MAP.entrySet()) {
				Location location = builderEntry.getKey();
				ScreenBuilder screenBuilder = builderEntry.getValue();
				if (positions.has(location.id())) {
					JsonObject locationObject = positions.getAsJsonObject(location.id());
					for (Map.Entry<String, JsonElement> entry : locationObject.entrySet()) {
						PositionRule.CODEC.decode(JsonOps.INSTANCE, entry.getValue())
								.ifSuccess(pair -> screenBuilder.setPositionRule(entry.getKey(), pair.getFirst()))
								.ifError(pairError -> LOGGER.error("[Skyblocker] Failed to parse position rule: {}", pairError.messageSupplier().get()));
					}
				}
			}
		} catch (NoSuchFileException e) {
			LOGGER.warn("[Skyblocker] No hud widget config file found, using defaults");
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to load hud widgets config", e);
		}*/
	}

	public static void saveConfig() {
		/*JsonObject output = new JsonObject();
		JsonObject positions = new JsonObject();
		for (Map.Entry<Location, ScreenBuilder> builderEntry : BUILDER_MAP.entrySet()) {
			Location location = builderEntry.getKey();
			ScreenBuilder screenBuilder = builderEntry.getValue();
			JsonObject locationObject = new JsonObject();
			screenBuilder.forEachPositionRuleEntry((s, positionRule) -> locationObject.add(s, PositionRule.CODEC.encodeStart(JsonOps.INSTANCE, positionRule).getOrThrow()));
			if (locationObject.isEmpty()) continue;
			positions.add(location.id(), locationObject);
		}
		output.add("positions", positions);
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			SkyblockerMod.GSON.toJson(output, writer);
			LOGGER.info("[Skyblocker] Saved hud widget config");
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to save hud widget config", e);
		}*/
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
	}

	/**
	 * @implNote !! The 3 first ones shouldn't be moved, ordinal is used in some places
	 */
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
