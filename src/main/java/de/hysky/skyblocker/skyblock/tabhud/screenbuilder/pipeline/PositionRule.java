package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

public record PositionRule(String parent, Point parentPoint, Point thisPoint, int relativeX, int relativeY) {


    public enum HorizontalPoint {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum VerticalPoint {
        TOP,
        CENTER,
        BOTTOM
    }

    public record Point(HorizontalPoint horizontalPoint, VerticalPoint verticalPoint) {}
}
