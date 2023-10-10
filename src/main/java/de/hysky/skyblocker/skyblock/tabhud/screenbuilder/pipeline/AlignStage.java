package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;

public class AlignStage extends PipelineStage {

    private enum AlignReference {
        HORICENT("horizontalCenter"),
        VERTCENT("verticalCenter"),
        LEFTCENT("leftOfCenter"),
        RIGHTCENT("rightOfCenter"),
        TOPCENT("topOfCenter"),
        BOTCENT("botOfCenter"),
        TOP("top"),
        BOT("bot"),
        LEFT("left"),
        RIGHT("right");

        private final String str;

        AlignReference(String d) {
            this.str = d;
        }

        public static AlignReference parse(String s) throws NoSuchElementException {
            for (AlignReference d : AlignReference.values()) {
                if (d.str.equals(s)) {
                    return d;
                }
            }
            throw new NoSuchElementException("\"" + s + "\" is not a valid reference for an align op!");
        }
    }

    private final AlignReference reference;

    public AlignStage(ScreenBuilder builder, JsonObject descr) {
        this.reference = AlignReference.parse(descr.get("reference").getAsString());
        this.primary = new ArrayList<>(descr.getAsJsonArray("apply_to")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
    }

    public void run(int screenW, int screenH) {
        int wHalf, hHalf;
        for (Widget wid : primary) {
            switch (this.reference) {
                case HORICENT -> wid.setX((screenW - wid.getWidth()) / 2);
                case VERTCENT -> wid.setY((screenH - wid.getHeight()) / 2);
                case LEFTCENT -> {
                    wHalf = screenW / 2;
                    wid.setX(wHalf - ScreenConst.WIDGET_PAD_HALF - wid.getWidth());
                }
                case RIGHTCENT -> {
                    wHalf = screenW / 2;
                    wid.setX(wHalf + ScreenConst.WIDGET_PAD_HALF);
                }
                case TOPCENT -> {
                    hHalf = screenH / 2;
                    wid.setY(hHalf - ScreenConst.WIDGET_PAD_HALF - wid.getHeight());
                }
                case BOTCENT -> {
                    hHalf = screenH / 2;
                    wid.setY(hHalf + ScreenConst.WIDGET_PAD_HALF);
                }
                case TOP -> wid.setY(ScreenConst.getScreenPad());
                case BOT -> wid.setY(screenH - wid.getHeight() - ScreenConst.getScreenPad());
                case LEFT -> wid.setX(ScreenConst.getScreenPad());
                case RIGHT -> wid.setX(screenW - wid.getWidth() - ScreenConst.getScreenPad());
            }
        }
    }

}
