package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.AlignStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CollideStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PipelineStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PlaceStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.StackStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ErrorWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EventWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class ScreenBuilder {

    // layout pipeline
    private ArrayList<PipelineStage> layoutPipeline = new ArrayList<>();

    // all widget instances this builder knows
    private ArrayList<Widget> instances = new ArrayList<>();
    // maps alias -> widget instance
    private HashMap<String, Widget> objectMap = new HashMap<>();

    private String builderName;

    /**
     * Create a ScreenBuilder from a json.
     */
    public ScreenBuilder(Identifier ident) {

        try (BufferedReader reader = MinecraftClient.getInstance().getResourceManager().openAsReader(ident);) {
            this.builderName = ident.getPath();

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray widgets = json.getAsJsonArray("widgets");
            JsonArray layout = json.getAsJsonArray("layout");

            for (JsonElement w : widgets) {
                JsonObject widget = w.getAsJsonObject();
                String name = widget.get("name").getAsString();
                String alias = widget.get("alias").getAsString();

                Widget wid = instanceFrom(name, widget);
                objectMap.put(alias, wid);
                instances.add(wid);
            }

            for (JsonElement l : layout) {
                PipelineStage ps = createStage(l.getAsJsonObject());
                layoutPipeline.add(ps);
            }
        } catch (Exception ex) {
            // rethrow as unchecked exception so that I don't have to catch anything in the ScreenMaster
            throw new IllegalStateException("Failed to load file " + ident + "Reason: " + ex.getMessage());
        }
    }

    /**
     * Try to find a class in the widget package that has the supplied name and
     * call it's constructor. Manual work is required if the class has arguments.
     */
    public Widget instanceFrom(String name, JsonObject widget) {

        // do widgets that require args the normal way
        JsonElement arg;
        switch (name) {
            case "EventWidget":
                return new EventWidget(widget.get("inGarden").getAsBoolean());

            case "DungeonPlayerWidget":
                return new DungeonPlayerWidget(widget.get("player").getAsInt());

            case "ErrorWidget":
                arg = widget.get("text");
                if (arg == null) {
                    return new ErrorWidget();
                } else {
                    return new ErrorWidget(arg.getAsString());
                }

            case "Widget":
                // clown case sanity check. don't instantiate the superclass >:|
                throw new NoSuchElementException(builderName + "[ERROR]: No such Widget type \"Widget\"!");
        }

        // reflect something together for the "normal" ones.
        // TODO don't get package list for every widget; do it once and cache.
        // fine for now, as this would only shorten the load time anyways

        // list all packages that might contain widget classes
        // using Package isn't reliable, as some classes might not be loaded yet,
        // causing the packages not to show.
        String packbase = "me.xmrvizzy.skyblocker.skyblock.tabhud.widget";
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
            return (Widget) ctor.newInstance();
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

        switch (op) {
            case "place":
                return new PlaceStage(this, descr);
            case "stack":
                return new StackStage(this, descr);
            case "align":
                return new AlignStage(this, descr);
            case "collideAgainst":
                return new CollideStage(this, descr);
            default:
                throw new NoSuchElementException("No such op " + op + " as requested by " + this.builderName);
        }
    }

    /**
     * Lookup Widget instance from alias name
     */
    public Widget getInstance(String name) {
        if (!this.objectMap.containsKey(name)) {
            throw new NoSuchElementException("No widget with alias " + name + " in screen " + builderName);
        }
        return this.objectMap.get(name);
    }

    /**
     * Run the pipeline to build a Screen
     */
    public void run(DrawContext context, int screenW, int screenH) {

        for (Widget w : instances) {
            w.update();
        }
        for (PipelineStage ps : layoutPipeline) {
            ps.run(screenW, screenH);
        }
        for (Widget w : instances) {
            w.render(context);
        }
    }

}
