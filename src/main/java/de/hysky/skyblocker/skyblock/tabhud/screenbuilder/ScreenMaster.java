package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ScreenMaster {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int VERSION = 2;
    private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("hud_widgets.json");

    private static Map<Location, ScreenBuilder> createBuilderMap() {
        EnumMap<Location, ScreenBuilder> map = new EnumMap<>(Location.class);
        for (Location value : Location.values()) {
            map.put(value, new ScreenBuilder(value));
        }
        return map;
    }

    private static final Map<Location, ScreenBuilder> builderMap = createBuilderMap();

    public static final Map<String, HudWidget> widgetInstances = new HashMap<>();

    public static ScreenBuilder getScreenBuilder(Location location) {
        return builderMap.get(location);
    }

    /**
     * Top level render method.
     * Calls the appropriate ScreenBuilder with the screen's dimensions
     * Called in PlayerListHudMixin
     */
    public static void render(DrawContext context, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        ScreenLayer screenLayer;
        if (client.options.playerListKey.isPressed()) {
            if (TabHud.defaultTgl.isPressed()) return;
            if (TabHud.toggleSecondary.isPressed()) {
                screenLayer = ScreenLayer.SECONDARY_TAB;
            } else {
                screenLayer = ScreenLayer.MAIN_TAB;
            }
        } else {
            screenLayer = ScreenLayer.HUD;
        }

        getScreenBuilder(Utils.getLocation()).run(context, w, h, screenLayer);
    }

    public static void loadConfig() {
        try (BufferedReader reader = Files.newBufferedReader(FILE)) {
            JsonObject object = SkyblockerMod.GSON.fromJson(reader, JsonObject.class);
            JsonObject positions = object.getAsJsonObject("positions");
            for (Map.Entry<Location, ScreenBuilder> builderEntry : builderMap.entrySet()) {
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
        }
    }

    public static void saveConfig() {
        JsonObject output = new JsonObject();
        JsonObject positions = new JsonObject();
        for (Map.Entry<Location, ScreenBuilder> builderEntry : builderMap.entrySet()) {
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
        }
    }

    // All non-tab HUDs should have a position rule initialised here, because they don't have an auto positioning
    private static void fillDefaultConfig() {
        ScreenBuilder screenBuilder = getScreenBuilder(Location.THE_END);
        screenBuilder.setPositionRule(
                "hud_end",
                new PositionRule("screen", PositionRule.Point.DEFAULT, PositionRule.Point.DEFAULT, SkyblockerConfigManager.get().otherLocations.end.x, SkyblockerConfigManager.get().otherLocations.end.y, ScreenMaster.ScreenLayer.HUD)
        );

        screenBuilder = getScreenBuilder(Location.GARDEN);
        screenBuilder.setPositionRule(
                "hud_farming",
                new PositionRule("screen", PositionRule.Point.DEFAULT, PositionRule.Point.DEFAULT, SkyblockerConfigManager.get().farming.garden.farmingHud.x, SkyblockerConfigManager.get().farming.garden.farmingHud.y, ScreenMaster.ScreenLayer.HUD)
        );

        for (Location loc : new Location[]{Location.CRYSTAL_HOLLOWS, Location.DWARVEN_MINES}) {
            screenBuilder = getScreenBuilder(loc);
            screenBuilder.setPositionRule(
                    "commissions",
                    new PositionRule("screen", PositionRule.Point.DEFAULT, PositionRule.Point.DEFAULT, 5, 5, ScreenMaster.ScreenLayer.HUD)
            );
            screenBuilder.setPositionRule(
                    "powders",
                    new PositionRule("commissions", new PositionRule.Point(PositionRule.VerticalPoint.BOTTOM, PositionRule.HorizontalPoint.LEFT), PositionRule.Point.DEFAULT, 0, 2, ScreenMaster.ScreenLayer.HUD)
            );

        }

    }

	// we probably want this to run pretty early?
    @Init(priority = -1)
    public static void init() {
        SkyblockEvents.LOCATION_CHANGE.register(location -> ScreenBuilder.positionsNeedsUpdating = true);

		instantiateWidgets();

		for (int i = 1; i < 6; i++) {
			DungeonPlayerWidget widget = new DungeonPlayerWidget(i);
			addWidgetInstance(widget);
			PlayerListMgr.tabWidgetInstances.put(widget.getHypixelWidgetName(), widget);
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            fillDefaultConfig();
            loadConfig();

        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());
    }


	private static void instantiateWidgets() {}

	@SuppressWarnings("unused")
	public static void addWidgetInstance(HudWidget widget) {
		HudWidget put = widgetInstances.put(widget.getInternalID(), widget);
		if (put != null) LOGGER.warn("[Skyblocker] Duplicate hud widget found: {}", widget);
	}

    /**
     * @implNote !! The 3 first ones shouldn't be moved, ordinal is used in some places
     */
    public enum ScreenLayer {
        MAIN_TAB,
        SECONDARY_TAB,
        HUD,
        /**
         * Default is only present for config and isn't used anywhere else
         */
        DEFAULT;

        public static final Codec<ScreenLayer> CODEC = Codec.STRING.xmap(ScreenLayer::valueOf, ScreenLayer::name);

        @Override
        public String toString() {
            return switch (this) {
                case MAIN_TAB -> "Main Tab";
                case SECONDARY_TAB -> "Secondary Tab";
                case HUD -> "HUD";
                case DEFAULT -> "Default";
            };
        }
    }

}
