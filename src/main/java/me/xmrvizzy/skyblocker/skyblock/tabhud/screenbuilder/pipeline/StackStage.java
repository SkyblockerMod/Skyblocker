package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class StackStage extends PipelineStage {

    private enum StackDirection {
        HORIZONTAL("horizontal"),
        VERTICAL("vertical");

        private String str;

        private StackDirection(String d) {
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

        private String str;

        private StackAlign(String d) {
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

    private StackDirection direction;
    private StackAlign align;

    public StackStage(ScreenBuilder builder, JsonObject descr) {
        this.direction = StackDirection.parse(descr.get("direction").getAsString());
        this.align = StackAlign.parse(descr.get("align").getAsString());
        this.primary = new ArrayList<Widget>(descr.getAsJsonArray("apply_to")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
    }

    public void run(int screenW, int screenH) {
        switch (this.direction) {
            case HORIZONTAL:
                stackWidgetsHoriz(screenW);
                break;
            case VERTICAL:
                stackWidgetsVert(screenH);
                break;
        }
    }

    public void stackWidgetsVert(int screenH) {
        int compHeight = -5;
        for (Widget wid : primary) {
            compHeight += wid.getHeight() + 5;
        }

        int y = switch (this.align) {

            case TOP -> y = 5;
            case BOT -> y = (screenH - compHeight) - 5;
            default -> y = (screenH - compHeight) / 2;
        };

        for (Widget wid : primary) {
            wid.setY(y);
            y += wid.getHeight() + 5;
        }
    }

    public void stackWidgetsHoriz(int screenW) {
        // TODO not centered (?)
        int compWidth = -5;
        for (Widget wid : primary) {
            compWidth += wid.getWidth() + 5;
        }

        int x = switch (this.align) {

            case LEFT-> x = 5;
            case RIGHT -> x = (screenW - compWidth) - 5;
            default -> x = (screenW - compWidth) / 2;
        };

        for (Widget wid : primary) {
            wid.setX(x);
            x += wid.getWidth() + 5;
        }
    }
}