package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class ScreenMaster {

    private static final Identifier ASSIGNMENT_JSON = new Identifier(SkyblockerMod.NAMESPACE, "tabhud/assignment.json");
    private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");

    private static HashMap<String, ScreenBuilder> standardMap = new HashMap<>();
    // private static HashMap<String, ScreenBuilder> screenAMap = new HashMap<>();
    // private static HashMap<String, ScreenBuilder> screenBMap = new HashMap<>();

    static {
        init();
    }

    /**
     * Load the assignment json and construct the screen mapping
     */
    public static void init() {
        try (BufferedReader reader = MinecraftClient.getInstance().getResourceManager().openAsReader(ASSIGNMENT_JSON)) {

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject standard = json.getAsJsonObject("standard");
            for (Entry<String, JsonElement> entry : standard.entrySet()) {
                standardMap.put(entry.getKey(), new ScreenBuilder(entry.getValue().getAsString()));
            }

        } catch (IOException ioex) {
            LOGGER.info("[Skyblocker] Couldn't load tabhud config!");
            ioex.printStackTrace();
        }
    }

    /**
     * Top level render method.
     * Calls the appropriate ScreenBuilder with the screen's dimensions
     */
    public static void render(DrawContext context, int w, int h) {
        standardMap.get("default").run(context, w, h);
    }
}

/*
 *
 * stackWidgetsH
stackWidgetsW
--> stack (direction?) horiz/vert (align?) center/top/bottom/left/right

centerH
centerW
center
--> center (center/horiz/vert)
----> place (one) (where?) center/top/bot/left/right/[corners]

offCenterL
offCenterR
--> offCenter left/right/top/bot
----> offsetPlace (where?) center/left/right/top/bot (offset to where?) left/right/top/bot 

----> align (any) (reference?) left, leftOfCenter, horizontalCenter, rightOfCenter, right, top, topOfCenter, verticalCenter, botOfCenter, bot

collideAgainstL
collideAgainstR
--> collideAgainst (from where?) left/right/top/bot
 */