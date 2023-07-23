package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class StackStage extends PipelineStage {

    private String direction;
    private String align;

    public StackStage(ScreenBuilder builder, JsonObject descr) {
        this.direction = descr.get("direction").getAsString();
        this.align = descr.get("align").getAsString();
        this.primary = new ArrayList<Widget>(descr.getAsJsonArray("apply_to")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
    }

    public void run(int screenW, int screenH) {
        switch (this.direction) {
            case "horizontal":
                stackWidgetsHoriz(screenW);
                break;
            case "vertical":
                stackWidgetsVert(screenH);
                break;
        }
    }

    public void stackWidgetsVert( int screenH) {
        int compHeight = -5;
        for (Widget wid : primary) {
            compHeight += wid.getHeight() + 5;
        }

        int y = switch (this.align) {

            case "top" -> y = 5;
            case "bot" -> y = (screenH-compHeight) - 5;
            default -> y = (screenH-compHeight)/2;
        };

        for (Widget wid : primary) {
            wid.setY(y);
            y += wid.getHeight() + 5;
        }
    }

    public void stackWidgetsHoriz(int screenW) {
        // TODO not centered
        int compWidth = -5;
        for (Widget wid : primary) {
            compWidth += wid.getWidth() + 5;
        }

        int x = switch (this.align) {

            case "left" -> x = 5;
            case "right" -> x = (screenW-compWidth) - 5;
            default -> x = (screenW-compWidth)/2;
        };

        for (Widget wid : primary) {
            wid.setX(x);
            x += wid.getWidth() + 5;
        }
    }
}