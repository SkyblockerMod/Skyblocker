package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public class CollideStage extends PipelineStage {

    private String direction;

    public CollideStage(ScreenBuilder builder, JsonObject descr) {
        this.direction = descr.get("direction").getAsString();
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
        if (this.direction.equals("left")) {
            primary.forEach(w -> collideAgainstL(screenW, w));
        } else if (this.direction.equals("right")) {
            primary.forEach(w -> collideAgainstR(screenW, w));
        }
    }

    public void collideAgainstL(int screenW, Widget w) {
        int yMin = w.getY();
        int yMax = w.getY() + w.getHeight();

        int xCor = screenW / 2;

        // assume others to be sorted top-bottom.
        for (Widget other : secondary) {
            if (other.getY() + other.getHeight() + 5 < yMin) {
                // too high, next one
                continue;
            }

            if (other.getY() - 5 > yMax) {
                // too low, next
                continue;
            }

            int xPos = other.getX() - 5 - w.getWidth();
            xCor = Math.min(xCor, xPos);
        }
        w.setX(xCor);
    }

    public void collideAgainstR(int screenW, Widget w) {
        int yMin = w.getY();
        int yMax = w.getY() + w.getHeight();

        int xCor = screenW / 2;

        // assume others to be sorted top-bottom.
        for (Widget other : secondary) {
            if (other.getY() + other.getHeight() + 5 < yMin) {
                // too high, next one
                continue;
            }

            if (other.getY() - 5 > yMax) {
                // too low, next
                continue;
            }

            int xPos = other.getX() + other.getWidth() + 5;
            xCor = Math.max(xCor, xPos);
        }
        w.setX(xCor);
    }

}
