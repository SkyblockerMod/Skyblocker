package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class AlignStage extends PipelineStage {

    private String reference;

    public AlignStage(ScreenBuilder builder, JsonObject descr) {
        this.reference = descr.get("reference").getAsString();
        this.primary = new ArrayList<Widget>(descr.getAsJsonArray("apply_to")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
    }

    public void run(int screenW, int screenH) {
        int wHalf, hHalf;
        for (Widget wid : primary) {
            switch (this.reference) {
                case "horizontalCenter":
                    wid.setX((screenW - wid.getWidth()) / 2);
                    break;
                case "verticalCenter":
                    wid.setY((screenH - wid.getHeight()) / 2);
                    break;
                case "leftOfCenter":
                    wHalf = screenW / 2;
                    wid.setX(wHalf - 3 - wid.getWidth());
                    break;
                case "rightOfCenter":
                    wHalf = screenW / 2;
                    wid.setX(wHalf + 3);
                    break;
                case "topOfCenter":
                    hHalf = screenH / 2;
                    wid.setY(hHalf - 3 - wid.getHeight());
                    break;
                case "botOfCenter":
                    hHalf = screenH / 2;
                    wid.setY(hHalf + 3);
                    break;
            }
        }
    }

}
