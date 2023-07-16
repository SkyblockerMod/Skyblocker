package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class PlaceStage extends PipelineStage {

    private String where;

    public PlaceStage(ScreenBuilder builder, JsonObject descr) {
        this.where = descr.get("where").getAsString();
        this.primary = new ArrayList<Widget>(descr.getAsJsonArray("apply_to")
            .asList()
            .stream()
            .map(x -> builder.getInstance(x.getAsString()))
            .toList());
    }

    public void run(int screenW, int screenH) {
        for (Widget wid : primary) {
            if (where.equals("center")) {
                wid.setY((screenH - wid.getHeight()) / 2);
                wid.setX((screenW - wid.getWidth()) / 2);
            }
        }
    }
}