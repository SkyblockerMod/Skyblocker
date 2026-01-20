package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DefaultClientPosArgument implements ClientPosArgument {
	private final WorldCoordinate x;
	private final WorldCoordinate y;
	private final WorldCoordinate z;

	public DefaultClientPosArgument(WorldCoordinate x, WorldCoordinate y, WorldCoordinate z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Vec3 toAbsolutePos(FabricClientCommandSource source) {
		Vec3 vec3d = source.getPosition();
		return new Vec3(this.x.get(vec3d.x), this.y.get(vec3d.y), this.z.get(vec3d.z));
	}

	@Override
	public Vec2 toAbsoluteRotation(FabricClientCommandSource source) {
		Vec2 vec2f = source.getRotation();
		return new Vec2((float) this.x.get(vec2f.x), (float) this.y.get(vec2f.y));
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
		WorldCoordinate coordinateArgument = WorldCoordinate.parseInt(reader);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			WorldCoordinate coordinateArgument2 = WorldCoordinate.parseInt(reader);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				WorldCoordinate coordinateArgument3 = WorldCoordinate.parseInt(reader);
				return new DefaultClientPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
			} else {
				reader.setCursor(i);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
		}
	}

	public static DefaultClientPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
		int i = reader.getCursor();
		WorldCoordinate coordinateArgument = WorldCoordinate.parseDouble(reader, centerIntegers);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			WorldCoordinate coordinateArgument2 = WorldCoordinate.parseDouble(reader, false);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				WorldCoordinate coordinateArgument3 = WorldCoordinate.parseDouble(reader, centerIntegers);
				return new DefaultClientPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
			} else {
				reader.setCursor(i);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
		}
	}

	public static DefaultClientPosArgument absolute(double x, double y, double z) {
		return new DefaultClientPosArgument(new WorldCoordinate(false, x), new WorldCoordinate(false, y), new WorldCoordinate(false, z));
	}

	public static DefaultClientPosArgument absolute(Vec2 vec) {
		return new DefaultClientPosArgument(new WorldCoordinate(false, vec.x), new WorldCoordinate(false, vec.y), new WorldCoordinate(true, 0.0));
	}

	public static DefaultClientPosArgument zero() {
		return new DefaultClientPosArgument(new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0));
	}

	public int hashCode() {
		int i = this.x.hashCode();
		i = 31 * i + this.y.hashCode();
		return 31 * i + this.z.hashCode();
	}
}
