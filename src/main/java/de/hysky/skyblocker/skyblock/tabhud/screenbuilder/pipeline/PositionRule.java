package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

public record PositionRule(String parent, Point parentPoint, Point thisPoint, int relativeX, int relativeY) {

    public static final PositionRule DEFAULT = new PositionRule("screen", Point.DEFAULT, Point.DEFAULT, 0, 0);


    public enum HorizontalPoint {
        LEFT,
        CENTER,
        RIGHT;

        public float getPercentage() {
            return switch (this) {
                case LEFT -> 0.f;
                case CENTER -> 0.5f;
                case RIGHT -> 1.f;
            };
        }
    }

    public enum VerticalPoint {
        TOP,
        CENTER,
        BOTTOM;

        public float getPercentage() {
            return switch (this) {
                case TOP -> 0.f;
                case CENTER -> 0.5f;
                case BOTTOM -> 1.f;
            };
        }
    }

    public record Point(VerticalPoint verticalPoint, HorizontalPoint horizontalPoint) {
        public static final Point DEFAULT = new Point(VerticalPoint.TOP, HorizontalPoint.LEFT);
    }
}
