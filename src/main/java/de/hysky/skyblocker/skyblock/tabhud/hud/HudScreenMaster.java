package de.hysky.skyblocker.skyblock.tabhud.hud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerLocator;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.util.Map;

public class HudScreenMaster extends ScreenMaster {

    /**
     * Load a screen mapping from an identifier
     */
    public static void load(Identifier ident) {
        String[] parts = ident.getPath().split("/");
        String screenType = parts[parts.length - 2];
        String location = parts[parts.length - 1].replace(".json", "");

        if (screenType.equals("hud"))
            standardMap.put(location, new ScreenBuilder(ident));
    }

    /**
     * Top level render method.
     * Calls the appropriate ScreenBuilder with the screen's dimensions
     */
    public static void render(DrawContext context, int w, int h) {
        String location = PlayerLocator.getPlayerLocation().internal;
        ScreenBuilder sb = standardMap.getOrDefault(location, standardMap.get("default"));
        if (sb != null) // Prevents flooding errors to the console.
            sb.run(context, w, h);
    }

    public static void init() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return new Identifier("skyblocker", "hud");
                    }

                    /**
                     * If the version.json file contains a format_version key
                     * which value is equal to {@link HudScreenMaster#VERSION}, then there are no errors.
                     *
                     * @param manager the Minecraft Resource manager
                     */
                    private static void validateResPackVersion(ResourceManager manager) {
                        for (Map.Entry<Identifier, Resource> entry : manager
                                .findResources("hud", path -> path.getPath().endsWith("version.json"))
                                .entrySet()) {

                            try (BufferedReader reader = MinecraftClient.getInstance().getResourceManager()
                                    .openAsReader(entry.getKey())) {
                                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                                if (json.get("format_version").getAsInt() != VERSION) {
                                    throw new IllegalStateException(String.format("Resource pack isn't compatible! Expected version %d, got %d", VERSION, json.get("format_version").getAsInt()));
                                }

                            } catch (Exception ex) {
                                throw new IllegalStateException("Rejected this resource pack. Reason: " + ex.getMessage());
                            }
                        }
                    }

                    /**
                     * Loads all json files in the hud folder except version.json.
                     *
                     * @param manager the Minecraft Resource manager
                     */
                    private static void loadResPackWidgets(ResourceManager manager) {
                        int excnt = 0;

                        for (Map.Entry<Identifier, Resource> entry : manager
                                .findResources("hud", path -> path.getPath().endsWith(".json") && !path.getPath().endsWith("version.json"))
                                .entrySet()) {
                            try {
                                load(entry.getKey());
                            } catch (Exception e) {
                                Hud.LOGGER.error(e.getMessage());
                                excnt++;
                            }
                        }

                        if (excnt > 0) {
                            throw new IllegalStateException("This screen definition isn't valid, see above");
                        }
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        standardMap.clear();
                        validateResPackVersion(manager);
                        loadResPackWidgets(manager);
                    }
                }
        );
    }

}
