package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerLocator;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class ScreenMaster {

    private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");

    private static final int VERSION = 1;

    private static final HashMap<String, ScreenBuilder> standardMap = new HashMap<>();
    private static final HashMap<String, ScreenBuilder> screenAMap = new HashMap<>();
    private static final HashMap<String, ScreenBuilder> screenBMap = new HashMap<>();

    /**
     * Load a screen mapping from an identifier
     */
    public static void load(Identifier ident) {

        String path = ident.getPath();
        String[] parts = path.split("/");
        String screenType = parts[parts.length - 2];
        String location = parts[parts.length - 1];
        location = location.replace(".json", "");

        ScreenBuilder sb = new ScreenBuilder(ident);
        switch (screenType) {
            case "standard" -> standardMap.put(location, sb);
            case "screen_a" -> screenAMap.put(location, sb);
            case "screen_b" -> screenBMap.put(location, sb);
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

    @Init
    public static void init() {

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
                                throw new IllegalStateException(
                                        "Rejected this resource pack. Reason: " + ex.getMessage());
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
                            throw new IllegalStateException("This screen definition isn't valid, see above");
                        }
                    }
                });
    }

}
