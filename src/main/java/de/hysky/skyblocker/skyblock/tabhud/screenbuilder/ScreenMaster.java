package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.common.reflect.ClassPath;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ScreenMaster {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int VERSION = 1;
    private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("hud_widgets.json");

    private static final Map<Location, ScreenBuilder> builderMap = new EnumMap<>(Location.class);

    public static final Map<String, HudWidget> widgetInstances = new HashMap<>();

    /**
     * Load a screen mapping from an identifier
     */
    public static void load(Identifier ident) {

        String path = ident.getPath();
        String[] parts = path.split("/");
        String screenType = parts[parts.length - 2];
        String location = parts[parts.length - 1];
        location = location.replace(".json", "");
    }

    public static ScreenBuilder getScreenBuilder(Location location) {
        return builderMap.get(location);
    }

    /**
     * Top level render method.
     * Calls the appropriate ScreenBuilder with the screen's dimensions
     */
    public static void render(DrawContext context, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        ScreenLayer screenLayer;
        if (client.options.playerListKey.isPressed()) {
            if (TabHud.defaultTgl.isPressed()) return;
            if (TabHud.toggleA.isPressed()) {
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
            
        } catch (NoSuchFileException e) {
            LOGGER.warn("[Skyblocker] No status bar config file found, using defaults");
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Failed to load hud widgets config", e);
        }
    }

    @Init
    public static void init() {

        SkyblockEvents.LOCATION_CHANGE.register(location -> ScreenBuilder.positionsNeedsUpdating = true);

        HudRenderEvents.BEFORE_CHAT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Window window = client.getWindow();
            float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f;
            render(context, (int) (window.getScaledWidth() / scale), (int) (window.getScaledHeight() / scale));
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                ClassPath.from(TabHudWidget.class.getClassLoader()).getTopLevelClasses("de.hysky.skyblocker.skyblock.tabhud.widget").iterator().forEachRemaining(classInfo -> {
                    try {
                        Class<?> load = Class.forName(classInfo.getName());
                        if (!load.getSuperclass().equals(TabHudWidget.class)) return;
                        TabHudWidget tabHudWidget = (TabHudWidget) load.getDeclaredConstructor().newInstance();
                        PlayerListMgr.tabWidgetInstances.put(tabHudWidget.getHypixelWidgetName(), tabHudWidget);
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                             IllegalAccessException | ClassNotFoundException e) {
                        LOGGER.error("[Skyblocker] Failed to load {} hud widget", classInfo.getName(), e);
                    }

                });
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to get instances of hud widgets", e);
            }

        });

        for (Location value : Location.values()) {
            builderMap.put(value, new ScreenBuilder(value));
        }
        /*


        // WHY MUST IT ALWAYS BE SUCH NESTED GARBAGE MINECRAFT KEEP THAT IN DFU FFS

        ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of(SkyblockerMod.NAMESPACE, "top_aligned"),
                SkyblockerMod.SKYBLOCKER_MOD,
                ResourcePackActivationType.NORMAL
        );

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                // ...why are we instantiating an interface again?
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(SkyblockerMod.NAMESPACE, "tabhud");
                    }

                    @Override
                    public void reload(ResourceManager manager) {

                        standardMap.clear();
                        screenAMap.clear();
                        screenBMap.clear();

                        int excnt = 0;

                        for (Map.Entry<Identifier, Resource> entry : manager
                                .findResources("tabhud", path -> path.getPath().endsWith("version.json"))
                                .entrySet()) {

                            try (BufferedReader reader = MinecraftClient.getInstance().getResourceManager()
                                    .openAsReader(entry.getKey())) {
                                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                                if (json.get("format_version").getAsInt() != VERSION) {
                                    throw new IllegalStateException(String.format("Resource pack isn't compatible! Expected version %d, got %d", VERSION, json.get("format_version").getAsInt()));
                                }

                            } catch (Exception ex) {
                                LOGGER.error("it borked", ex);
                            }
                        }

                        for (Map.Entry<Identifier, Resource> entry : manager
                                .findResources("tabhud", path -> path.getPath().endsWith(".json") && !path.getPath().endsWith("version.json"))
                                .entrySet()) {
                            try {

                                load(entry.getKey());
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage());
                                excnt++;
                            }
                        }
                        if (excnt > 0) {
                            LOGGER.warn("shit went down");
                        }
                    }
                });

         */
    }

    public enum ScreenLayer {
        MAIN_TAB,
        SECONDARY_TAB,
        HUD;

        public static final Codec<ScreenLayer> CODEC_NULLABLE = Codec.STRING.xmap(
                s -> s.equals("null") ? null : ScreenLayer.valueOf(s),
                screenLayer -> screenLayer == null ? "null": screenLayer.name());

        @Override
        public String toString() {
            return switch (this) {
                case MAIN_TAB -> "Main Tab";
                case SECONDARY_TAB -> "Secondary Tab";
                case HUD -> "HUD";
            };
        }
    }

}
