package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.ScreenConst;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class CollideStage extends PipelineStage {

    private enum CollideDirection {
        LEFT("left"),
        RIGHT("right"),
        TOP("top"),
        BOT("bot");

        private String str;

        private CollideDirection(String d) {
            this.str = d;
        }

        public static CollideDirection parse(String s) throws NoSuchElementException {
            for (CollideDirection d : CollideDirection.values()) {
                if (d.str.equals(s)) {
                    return d;
                }
            }
            throw new NoSuchElementException("\"" + s + "\" is not a valid direction for a collide op!");
        }
    }

    private CollideDirection direction;

    public CollideStage(ScreenBuilder builder, JsonObject descr) {
        this.direction = CollideDirection.parse(descr.get("direction").getAsString());
        this.primary = new ArrayList<Widget>(descr.getAsJsonArray("widgets")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
        this.secondary = new ArrayList<Widget>(descr.getAsJsonArray("colliders")
                .asList()
                .stream()
                .map(x -> builder.getInstance(x.getAsString()))
                .toList());
    }

    public void run(int screenW, int screenH) {
        switch (this.direction) {
            case LEFT:
                primary.forEach(w -> collideAgainstL(screenW, w));
                break;
            case RIGHT:
                primary.forEach(w -> collideAgainstR(screenW, w));
                break;
            case TOP:
                primary.forEach(w -> collideAgainstT(screenH, w));
                break;
            case BOT:
                primary.forEach(w -> collideAgainstB(screenH, w));
                break;
        }
    }

    public void collideAgainstL(int screenW, Widget w) {
        int yMin = w.getY();
        int yMax = w.getY() + w.getHeight();

        int xCor = screenW;

        for (Widget other : secondary) {
            if (other.getY() + other.getHeight() + ScreenConst.WIDGET_PAD < yMin) {
                // too high, next one
                continue;
            }

            if (other.getY() - ScreenConst.WIDGET_PAD > yMax) {
                // too low, next
                continue;
            }

            int xPos = other.getX() - ScreenConst.WIDGET_PAD - w.getWidth();
            xCor = Math.min(xCor, xPos);
        }
        w.setX(xCor);
    }

    public void collideAgainstR(int screenW, Widget w) {
        int yMin = w.getY();
        int yMax = w.getY() + w.getHeight();

        int xCor = 0;

        for (Widget other : secondary) {
            if (other.getY() + other.getHeight() + ScreenConst.WIDGET_PAD < yMin) {
                // too high, next one
                continue;
            }

            if (other.getY() - ScreenConst.WIDGET_PAD > yMax) {
                // too low, next
                continue;
            }

            int xPos = other.getX() + other.getWidth() + ScreenConst.WIDGET_PAD;
            xCor = Math.max(xCor, xPos);
        }
        w.setX(xCor);
    }

    public void collideAgainstT(int screenH, Widget w) {
        int xMin = w.getX();
        int xMax = w.getX() + w.getWidth();

        int yCor = screenH;

        for (Widget other : secondary) {
            if (other.getX() + other.getWidth() + ScreenConst.WIDGET_PAD < xMin) {
                // too far left, next one
                continue;
            }

            if (other.getX() - ScreenConst.WIDGET_PAD > xMax) {
                // too far right, next
                continue;
            }

            int yPos = other.getY() - ScreenConst.WIDGET_PAD - w.getHeight();
            yCor = Math.min(yCor, yPos);
        }
        w.setY(yCor);
    }

    public void collideAgainstB(int screenH, Widget w) {
        int xMin = w.getX();
        int xMax = w.getX() + w.getWidth();

        int yCor = 0;

        for (Widget other : secondary) {
            if (other.getX() + other.getWidth() + ScreenConst.WIDGET_PAD < xMin) {
                // too far left, next one
                continue;
            }

            if (other.getX() - ScreenConst.WIDGET_PAD > xMax) {
                // too far right, next
                continue;
            }

            int yPos = other.getY() + other.getHeight() + ScreenConst.WIDGET_PAD;
            yCor = Math.max(yCor, yPos);
        }
        w.setY(yCor);
    }

}
