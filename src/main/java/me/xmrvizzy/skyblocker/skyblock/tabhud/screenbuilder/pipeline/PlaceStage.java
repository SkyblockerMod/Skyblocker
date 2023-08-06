package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.ScreenConst;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class PlaceStage extends PipelineStage {

 private enum PlaceLocation {
        CENTER("center"),
        TOPCENT("centerTop");

        private String str;

        private PlaceLocation(String d) {
            this.str = d;
        }

        public static PlaceLocation parse(String s) throws NoSuchElementException {
            for (PlaceLocation d : PlaceLocation.values()) {
                if (d.str.equals(s)) {
                    return d;
                }
            }
            throw new NoSuchElementException("\"" + s + "\" is not a valid location for a place op!");
        }
    }


    private PlaceLocation where;

    public PlaceStage(ScreenBuilder builder, JsonObject descr) {
        this.where = PlaceLocation.parse(descr.get("where").getAsString());
        this.primary = new ArrayList<Widget>(descr.getAsJsonArray("apply_to")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .limit(1)
                .toList());
    }

    public void run(int screenW, int screenH) {
        Widget wid = primary.get(0);
        switch (where) {
            case CENTER:
                wid.setY((screenH - wid.getHeight()) / 2);
                wid.setX((screenW - wid.getWidth()) / 2);
                break;
            case TOPCENT:
                wid.setX((screenW - wid.getWidth()) / 2);
                wid.setY(ScreenConst.SCREEN_PAD);
                break;
        }
    }
}