package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import java.io.BufferedReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;
import de.hysky.skyblocker.skyblock.tabhud.widget.*;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.AlignStage;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CollideStage;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PipelineStage;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PlaceStage;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.StackStage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ScreenBuilder {

    public static boolean positionsNeedsUpdating = true;

    // layout pipeline
    private final ArrayList<PipelineStage> layoutPipeline = new ArrayList<>();

    // all widget instances this builder knows
    private final ArrayList<HudWidget> instances = new ArrayList<>();
    // maps alias -> widget instance
    private final HashMap<String, HudWidget> objectMap = new HashMap<>();

    private final String builderName;

    private final Map<String, Boolean> positioning = new Object2ObjectOpenHashMap<>();

    /**
     * Create a ScreenBuilder from a json.
     */
    public ScreenBuilder(Identifier ident) {

        try (BufferedReader reader = MinecraftClient.getInstance().getResourceManager().openAsReader(ident)) {
            this.builderName = ident.getPath();

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray widgets = json.getAsJsonArray("widgets");
            JsonArray layout = json.getAsJsonArray("layout");

            for (JsonElement w : widgets) {
                JsonObject widget = w.getAsJsonObject();
                String name = widget.get("name").getAsString();
                String alias = widget.get("alias").getAsString();

                HudWidget wid = instanceFrom(name, widget);
                objectMap.put(alias, wid);
                instances.add(wid);
            }

            for (JsonElement l : layout) {
                PipelineStage ps = createStage(l.getAsJsonObject());
                layoutPipeline.add(ps);
            }
        } catch (Exception ex) {
            // rethrow as unchecked exception so that I don't have to catch anything in the ScreenMaster
            throw new IllegalStateException("Failed to load file " + ident + ". Reason: " + ex.getMessage());
        }
    }

    /**
     * Try to find a class in the widget package that has the supplied name and
     * call it's constructor. Manual work is required if the class has arguments.
     */
    public HudWidget instanceFrom(String name, JsonObject widget) {

        // do widgets that require args the normal way
        JsonElement arg;
        switch (name) {
            case "EventWidget" -> {
                return new EventWidget(widget.get("inGarden").getAsBoolean());
            }
            case "DungeonPlayerWidget" -> {
                return new DungeonPlayerWidget(widget.get("player").getAsInt());
            }
            case "ErrorWidget" -> {
                arg = widget.get("text");
                if (arg == null) {
                    return new ErrorWidget();
                } else {
                    return new ErrorWidget(arg.getAsString());
                }
            }
            case "Widget" ->
                // clown case sanity check. don't instantiate the superclass >:|
                    throw new NoSuchElementException(builderName + "[ERROR]: No such Widget type \"Widget\"!");
        }

        // reflect something together for the "normal" ones.

        // list all packages that might contain widget classes
        // using Package isn't reliable, as some classes might not be loaded yet,
        // causing the packages not to show.
        String packbase = "de.hysky.skyblocker.skyblock.tabhud.widget";
        String[] packnames = {
                packbase,
                packbase + ".rift"
        };

        // construct the full class name and try to load.
        Class<?> clazz = null;
        for (String pn : packnames) {
            try {
                clazz = Class.forName(pn + "." + name);
            } catch (LinkageError | ClassNotFoundException ex) {
                continue;
            }
        }

        // load failed.
        if (clazz == null) {
           throw new NoSuchElementException(builderName + "/[ERROR]: No such Widget type \"" + name + "\"!");
        }

        // return instance of that class.
        try {
            Constructor<?> ctor = clazz.getConstructor();
            return (HudWidget) ctor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
            throw new IllegalStateException(builderName + "/" + name + ": Internal error...");
        }
    }

    /**
     * Create a PipelineStage from a json object.
     */
    public PipelineStage createStage(JsonObject descr) throws NoSuchElementException {

        String op = descr.get("op").getAsString();

        return switch (op) {
            case "place" -> new PlaceStage(this, descr);
            case "stack" -> new StackStage(this, descr);
            case "align" -> new AlignStage(this, descr);
            case "collideAgainst" -> new CollideStage(this, descr);
            default -> throw new NoSuchElementException("No such op " + op + " as requested by " + this.builderName);
        };
    }

    /**
     * Lookup Widget instance from alias name
     */
    public HudWidget getInstance(String name) {
        if (!this.objectMap.containsKey(name)) {
            throw new NoSuchElementException("No widget with alias " + name + " in screen " + builderName);
        }
        return this.objectMap.get(name);
    }

    private static int totalWidth = 0;

    /**
     * Run the pipeline to build a Screen
     */
    public void run(DrawContext context, int screenW, int screenH) {

        if (positionsNeedsUpdating) {

            positionsNeedsUpdating = false;
            final int maxY = 200;
            final int startY = 20;

            totalWidth = 0;

            int currentWidth = 0;
            int currentY = startY;
            for (TabHudWidget tabHudWidget : PlayerListMgr.widgetsToShow) {
                if (positioning.getOrDefault(tabHudWidget.getInternalID(), false)) continue;
                if (currentY + tabHudWidget.getHeight() > maxY) {
                    totalWidth += currentWidth + ScreenConst.WIDGET_PAD;
                    currentY = startY;
                    currentWidth = 0;
                }
                tabHudWidget.setPosition(totalWidth, currentY);
                currentY += tabHudWidget.getHeight() + ScreenConst.WIDGET_PAD;
                currentWidth = Math.max(currentWidth, tabHudWidget.getWidth());
            }
            totalWidth += currentWidth;
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) (screenW - totalWidth)/2, 0, 0);
        for (HudWidget w : instances) {
            w.render(context);
        }
        matrices.pop();
    }

}
