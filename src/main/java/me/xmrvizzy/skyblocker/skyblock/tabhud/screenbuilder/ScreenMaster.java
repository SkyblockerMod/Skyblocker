package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.xmrvizzy.skyblocker.skyblock.tabhud.TabHud;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerLocator;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class ScreenMaster {

    private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");

    private static HashMap<String, ScreenBuilder> standardMap = new HashMap<>();
    private static HashMap<String, ScreenBuilder> screenAMap = new HashMap<>();
    private static HashMap<String, ScreenBuilder> screenBMap = new HashMap<>();

    /**
     * Load a screen mapping from
     */
    public static void load(Identifier ident) {

        String path = ident.getPath();
        String[] parts = path.split("/");
        String screenType = parts[parts.length - 2];
        String location = parts[parts.length - 1];
        location = location.replace(".json", "");

        try {

            if (screenType.equals("standard")) {
                standardMap.put(location, new ScreenBuilder(ident));
            } else if (screenType.equals("screenA")) {
                screenAMap.put(location, new ScreenBuilder(ident));
            } else if (screenType.equals("screenB")) {
                screenBMap.put(location, new ScreenBuilder(ident));
            }
        } catch (IOException ioex) {
            LOGGER.error("Can't load screen definition from {}", path);
        }

    }

    /**
     * Top level render method.
     * Calls the appropriate ScreenBuilder with the screen's dimensions
     */
    public static void render(DrawContext context, int w, int h) {
        String location = PlayerLocator.getPlayerLocation().internal;
        HashMap<String, ScreenBuilder> lookup;
        if (TabHud.toggleA.isPressed()) {
            lookup = screenAMap;
        } else if (TabHud.toggleB.isPressed()) {
            lookup = screenBMap;
        } else {
            lookup = standardMap;
        }

        ScreenBuilder sb = lookup.get(location);
        // seems suboptimal, maybe load the default first into all possible values
        // and then override?
        if (sb == null) {
            sb = lookup.get("default");
        }

        sb.run(context, w, h);

    }

    public static void init() {

        // WHY MUST IT ALWAYS BE SUCH NESTED GARBAGE MINECRAFT KEEP THAT IN DFU FFS

        FabricLoader.getInstance()
                .getModContainer("skyblocker")
                .ifPresent(container -> ResourceManagerHelper.registerBuiltinResourcePack(
                        new Identifier("skyblocker", "default_top"),
                        container,
                        ResourcePackActivationType.NORMAL));

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                // ...why are we instantiating an interface again?
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return new Identifier("skyblocker", "tabhud");
                    }

                    @Override
                    public void reload(ResourceManager manager) {

                        standardMap.clear();
                        screenAMap.clear();
                        screenBMap.clear();

                        for (Map.Entry<Identifier, Resource> entry : manager
                                .findResources("tabhud", path -> path.getPath().endsWith(".json"))
                                .entrySet()) {
                            load(entry.getKey());
                        }
                    }
                });
    }

}
