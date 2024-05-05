package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;

public class StackStage extends PipelineStage {

    private enum StackDirection {
        HORIZONTAL("horizontal"),
        VERTICAL("vertical");

        private final String str;

        StackDirection(String d) {
            this.str = d;
        }

        public static StackDirection parse(String s) throws NoSuchElementException {
            for (StackDirection d : StackDirection.values()) {
                if (d.str.equals(s)) {
                    return d;
                }
            }
            throw new NoSuchElementException("\"" + s + "\" is not a valid direction for a stack op!");
        }
    }

    private enum StackAlign {
        TOP("top"),
        BOT("bot"),
        LEFT("left"),
        RIGHT("right"),
        CENTER("center");

        private final String str;

        StackAlign(String d) {
            this.str = d;
        }

        public static StackAlign parse(String s) throws NoSuchElementException {
            for (StackAlign d : StackAlign.values()) {
                if (d.str.equals(s)) {
                    return d;
                }
            }
            throw new NoSuchElementException("\"" + s + "\" is not a valid alignment for a stack op!");
        }
    }

    private final StackDirection direction;
    private final StackAlign align;

    public StackStage(ScreenBuilder builder, JsonObject descr) {
        this.direction = StackDirection.parse(descr.get("direction").getAsString());
        this.align = StackAlign.parse(descr.get("align").getAsString());
        this.primary = new ArrayList<>(descr.getAsJsonArray("apply_to")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
    }

    public void run(int screenW, int screenH) {
        switch (this.direction) {
            case HORIZONTAL -> stackWidgetsHoriz(screenW);
            case VERTICAL -> stackWidgetsVert(screenH);
        }
    }

    public void stackWidgetsVert(int screenH) {
        int compHeight = -ScreenConst.WIDGET_PAD;
        for (HudWidget wid : primary) {
            compHeight += wid.getHeight() + 5;
        }

        int y = switch (this.align) {

            case TOP -> ScreenConst.getScreenPad();
            case BOT -> (screenH - compHeight) - ScreenConst.getScreenPad();
            default -> (screenH - compHeight) / 2;
        };

        for (HudWidget wid : primary) {
            wid.setY(y);
            y += wid.getHeight() + ScreenConst.WIDGET_PAD;
        }
    }

    public void stackWidgetsHoriz(int screenW) {
        int compWidth = -ScreenConst.WIDGET_PAD;
        for (HudWidget wid : primary) {
            compWidth += wid.getWidth() + ScreenConst.WIDGET_PAD;
        }

        int x = switch (this.align) {

            case LEFT -> ScreenConst.getScreenPad();
            case RIGHT -> (screenW - compWidth) - ScreenConst.getScreenPad();
            default -> (screenW - compWidth) / 2;
        };

        for (HudWidget wid : primary) {
            wid.setX(x);
            x += wid.getWidth() + ScreenConst.WIDGET_PAD;
        }
    }
}