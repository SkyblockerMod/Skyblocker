package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.AlignStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CollideStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PipelineStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PlaceStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline.StackStage;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EmptyWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EventWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class ScreenBuilder {

    // TODO: Let EmptyWidget contain an error message

    private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");
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
    public ScreenBuilder(Identifier ident) throws IOException {

        this.builderName = ident.getPath();

        BufferedReader reader = MinecraftClient.getInstance().getResourceManager().openAsReader(ident);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();

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
            if (ps != null) {
                layoutPipeline.add(ps);
            }
        }

    }

    /**
     * Try to find a class in the widget package that has the supplied name and
     * call it's constructor. Manual work is required if the class has arguments.
     */
    public Widget instanceFrom(String name, JsonObject widget) {

        // do widgets that require args the normal way
        switch (name) {
            case "EventWidget":
                return new EventWidget(widget.get("inGarden").getAsBoolean());
            case "DungeonPlayerWidget":
                return new DungeonPlayerWidget(widget.get("player").getAsInt());
            case "Widget":
                // clown case sanity check. don't instantiate the superclass >:|
                LOGGER.error("Couldn't find class \"{}\"!", name);
                return new EmptyWidget();
        }

        // reflect something together for the "normal" ones.
        // TODO don't get package list for every widget; do it once and cache.
        // fine for now, as this would only shorten the load time anyways

        // list all packages that might contain widget classes
        // using Package isn't reliable, as some classes might not be loaded yet,
        //   causing the packages not to show.
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
            LOGGER.error("Couldn't find class \"{}\"!", name);
            return new EmptyWidget();
        }

        // return instance of that class.
        try {
            Constructor<?> ctor = clazz.getConstructor();
            return (Widget) ctor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
            LOGGER.error("Failed to create instance of class {}!", clazz.getSimpleName());
            return new EmptyWidget();
        }
    }

    /**
     * Create a PipelineStage from a json object.
     */
    public PipelineStage createStage(JsonObject descr) {

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
                LOGGER.error("No such op \"{}\" as requested by {}", op, this.builderName);
                return null;
        }
    }

    /**
     * Lookup Widget instance from alias name
     */
    public Widget getInstance(String name) {
        // TODO: filter null here or in stage classes
        return this.objectMap.get(name);
    }

    /**
     * Run the pipeline to build a Screen
     */
    public void run(DrawContext context, int screenW, int screenH) {
        // TODO for future:
        // no need to update and run pipeline if PlayerListMgr wasn't updated.

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
