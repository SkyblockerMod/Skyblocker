package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;

public record PositionRule(String parent, Point parentPoint, Point thisPoint, int relativeX, int relativeY,
						WidgetManager.ScreenLayer screenLayer) {
	public static final PositionRule DEFAULT = new PositionRule("screen", Point.DEFAULT, Point.DEFAULT, 5, 5, WidgetManager.ScreenLayer.DEFAULT);
	public static final Codec<PositionRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("parent").forGetter(PositionRule::parent),
			Point.CODEC.fieldOf("parent_anchor").forGetter(PositionRule::parentPoint),
			Point.CODEC.fieldOf("this_anchor").forGetter(PositionRule::thisPoint),
			Codec.INT.fieldOf("relative_x").forGetter(PositionRule::relativeX),
			Codec.INT.fieldOf("relative_y").forGetter(PositionRule::relativeY),
			WidgetManager.ScreenLayer.CODEC.fieldOf("layer").forGetter(PositionRule::screenLayer)
	).apply(instance, PositionRule::new));


	public enum HorizontalPoint {
		LEFT,
		CENTER,
		RIGHT;

		public static final Codec<HorizontalPoint> CODEC = Codec.STRING.xmap(HorizontalPoint::valueOf, HorizontalPoint::name);

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

		public static final Codec<VerticalPoint> CODEC = Codec.STRING.xmap(VerticalPoint::valueOf, VerticalPoint::name);

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
		public static final Codec<Point> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				VerticalPoint.CODEC.fieldOf("v").forGetter(Point::verticalPoint),
				HorizontalPoint.CODEC.fieldOf("h").forGetter(Point::horizontalPoint)
		).apply(instance, Point::new));
	}
}
