package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EmptyWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ScreenBuilder {

    private ArrayList<PipelineStage> layoutPipeline = new ArrayList<>();

    private ArrayList<Widget> instances = new ArrayList<>();
    private HashMap<String, Widget> objectMap = new HashMap<>();


    public ScreenBuilder(String jsonfile) throws IOException {

        Identifier ident = new Identifier(SkyblockerMod.NAMESPACE, "tabhud/" + jsonfile + ".json");
        BufferedReader reader = MinecraftClient.getInstance().getResourceManager().openAsReader(ident);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();

        JsonArray widgets = json.getAsJsonArray("widgets");
        JsonArray layout = json.getAsJsonArray("layout");

        for (JsonElement w : widgets) {
            JsonObject widget = w.getAsJsonObject();
            String name = widget.get("name").getAsString();
            String alias = widget.get("alias").getAsString();

            JsonElement args = widget.get("args");
            JsonObject argsObj = (args == null) ? null : args.getAsJsonObject();

            Widget wid = instanceFrom(name, argsObj);
            objectMap.put(alias, wid);
            instances.add(wid);
        }

        for (JsonElement l : layout) {
            layoutPipeline.add(createStage(l.getAsJsonObject()));
        }

    }

    public Widget instanceFrom(String name, JsonObject args) {
        return switch (name) {
            case "EmptyWidget" -> new EmptyWidget();
            default -> new EmptyWidget();
        };
    }

    public PipelineStage createStage(JsonObject descr) {

        String op = descr.get("op").getAsString();

        switch (op) {
        case "place":
                return new PlaceStage(this, descr);
        }
        return null;
    }

    public Widget getInstance(String name) {
        return this.objectMap.get(name);
    }

    public void run(DrawContext context, int screenW, int screenH, Text footer) {

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
