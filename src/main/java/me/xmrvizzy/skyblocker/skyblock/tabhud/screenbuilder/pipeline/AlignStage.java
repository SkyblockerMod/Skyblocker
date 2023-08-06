package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.ScreenConst;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

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

        private String str;

        private AlignReference(String d) {
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

    private AlignReference reference;

    public AlignStage(ScreenBuilder builder, JsonObject descr) {
        this.reference = AlignReference.parse(descr.get("reference").getAsString());
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
                case HORICENT:
                    wid.setX((screenW - wid.getWidth()) / 2);
                    break;
                case VERTCENT:
                    wid.setY((screenH - wid.getHeight()) / 2);
                    break;
                case LEFTCENT:
                    wHalf = screenW / 2;
                    wid.setX(wHalf - ScreenConst.WIDGET_PAD_HALF - wid.getWidth());
                    break;
                case RIGHTCENT:
                    wHalf = screenW / 2;
                    wid.setX(wHalf + ScreenConst.WIDGET_PAD_HALF);
                    break;
                case TOPCENT:
                    hHalf = screenH / 2;
                    wid.setY(hHalf - ScreenConst.WIDGET_PAD_HALF - wid.getHeight());
                    break;
                case BOTCENT:
                    hHalf = screenH / 2;
                    wid.setY(hHalf + ScreenConst.WIDGET_PAD_HALF);
                    break;
                case TOP:
                    wid.setY(ScreenConst.getScreenPad());
                    break;
                case BOT:
                    wid.setY(screenH - wid.getHeight() - ScreenConst.getScreenPad());
                    break;
                case LEFT:
                    wid.setX(ScreenConst.getScreenPad());
                    break;
                case RIGHT:
                    wid.setX(screenW - wid.getWidth() - ScreenConst.getScreenPad());
                    break;
            }
        }
    }

}
