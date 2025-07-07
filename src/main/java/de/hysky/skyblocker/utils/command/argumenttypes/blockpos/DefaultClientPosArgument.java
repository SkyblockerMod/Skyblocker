package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DefaultClientPosArgument implements ClientPosArgument {
	private final CoordinateArgument x;
	private final CoordinateArgument y;
	private final CoordinateArgument z;

	public DefaultClientPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Vec3d toAbsolutePos(FabricClientCommandSource source) {
		Vec3d vec3d = source.getPosition();
		return new Vec3d(this.x.toAbsoluteCoordinate(vec3d.x), this.y.toAbsoluteCoordinate(vec3d.y), this.z.toAbsoluteCoordinate(vec3d.z));
	}

	@Override
	public Vec2f toAbsoluteRotation(FabricClientCommandSource source) {
		Vec2f vec2f = source.getRotation();
		return new Vec2f((float) this.x.toAbsoluteCoordinate(vec2f.x), (float) this.y.toAbsoluteCoordinate(vec2f.y));
	}

	@Override
	public boolean isXRelative() {
		return this.x.isRelative();
	}

	@Override
	public boolean isYRelative() {
		return this.y.isRelative();
	}

	@Override
	public boolean isZRelative() {
		return this.z.isRelative();
	}

	public boolean equals(Object o) {
		if (this == o) return true;

		return o instanceof DefaultClientPosArgument defaultPosArgument &&
				this.x.equals(defaultPosArgument.x) && this.y.equals(defaultPosArgument.y) && this.z.equals(defaultPosArgument.z);
	}

	public static DefaultClientPosArgument parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();
		CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader);
				return new DefaultClientPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
			} else {
				reader.setCursor(i);
				throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
		}
	}

	public static DefaultClientPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
		int i = reader.getCursor();
		CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader, centerIntegers);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader, false);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader, centerIntegers);
				return new DefaultClientPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
			} else {
				reader.setCursor(i);
				throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
		}
	}

	public static DefaultClientPosArgument absolute(double x, double y, double z) {
		return new DefaultClientPosArgument(new CoordinateArgument(false, x), new CoordinateArgument(false, y), new CoordinateArgument(false, z));
	}

	public static DefaultClientPosArgument absolute(Vec2f vec) {
		return new DefaultClientPosArgument(new CoordinateArgument(false, vec.x), new CoordinateArgument(false, vec.y), new CoordinateArgument(true, 0.0));
	}

	public static DefaultClientPosArgument zero() {
		return new DefaultClientPosArgument(new CoordinateArgument(true, 0.0), new CoordinateArgument(true, 0.0), new CoordinateArgument(true, 0.0));
	}

	public int hashCode() {
		int i = this.x.hashCode();
		i = 31 * i + this.y.hashCode();
		return 31 * i + this.z.hashCode();
	}
}
