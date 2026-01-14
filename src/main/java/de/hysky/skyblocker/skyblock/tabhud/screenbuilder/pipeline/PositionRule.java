package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record PositionRule(Optional<String> parent, Point parentPoint, Point thisPoint, int relativeX, int relativeY) {
	public static final PositionRule DEFAULT = new PositionRule(Optional.empty(), Point.DEFAULT, Point.DEFAULT, 5, 5);
	public static final Codec<PositionRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("parent").forGetter(PositionRule::parent),
			Point.CODEC.fieldOf("parent_anchor").forGetter(PositionRule::parentPoint),
			Point.CODEC.fieldOf("this_anchor").forGetter(PositionRule::thisPoint),
			Codec.INT.fieldOf("relative_x").forGetter(PositionRule::relativeX),
			Codec.INT.fieldOf("relative_y").forGetter(PositionRule::relativeY)
	).apply(instance, PositionRule::new));

	public PositionRule(String parent, Point parentPoint, Point thisPoint, int relativeX, int relativeY) {
		this(parent.equals("screen") ? Optional.empty() : Optional.of(parent), parentPoint, thisPoint, relativeX, relativeY);
	}


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
