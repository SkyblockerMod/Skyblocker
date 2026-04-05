package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;

public class LookingClientPosArgument implements ClientPosArgument {
	private final double x;
	private final double y;
	private final double z;

	public LookingClientPosArgument(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Vec3 toAbsolutePos(FabricClientCommandSource source) {
		Vec2 vec2f = source.getRotation();
		Vec3 vec3d = source.getPlayer().position();
		float f = Mth.cos((vec2f.y + 90.0F) * (float) (Math.PI / 180.0));
		float g = Mth.sin((vec2f.y + 90.0F) * (float) (Math.PI / 180.0));
		float h = Mth.cos(-vec2f.x * (float) (Math.PI / 180.0));
		float i = Mth.sin(-vec2f.x * (float) (Math.PI / 180.0));
		float j = Mth.cos((-vec2f.x + 90.0F) * (float) (Math.PI / 180.0));
		float k = Mth.sin((-vec2f.x + 90.0F) * (float) (Math.PI / 180.0));
		Vec3 vec3d2 = new Vec3(f * h, i, g * h);
		Vec3 vec3d3 = new Vec3(f * j, k, g * j);
		Vec3 vec3d4 = vec3d2.cross(vec3d3).scale(-1.0);
		double d = vec3d2.x * this.z + vec3d3.x * this.y + vec3d4.x * this.x;
		double e = vec3d2.y * this.z + vec3d3.y * this.y + vec3d4.y * this.x;
		double l = vec3d2.z * this.z + vec3d3.z * this.y + vec3d4.z * this.x;
		return new Vec3(vec3d.x + d, vec3d.y + e, vec3d.z + l);
	}

	@Override
	public Vec2 toAbsoluteRotation(FabricClientCommandSource source) {
		return Vec2.ZERO;
	}

	@Override
	public boolean isXRelative() {
		return true;
	}

	@Override
	public boolean isYRelative() {
		return true;
	}

	@Override
	public boolean isZRelative() {
		return true;
	}

	public static LookingClientPosArgument parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();
		double d = readCoordinate(reader, i);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			double e = readCoordinate(reader, i);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				double f = readCoordinate(reader, i);
				return new LookingClientPosArgument(d, e, f);
			} else {
				reader.setCursor(i);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
		}
	}

	private static double readCoordinate(StringReader reader, int startingCursorPos) throws CommandSyntaxException {
		if (!reader.canRead()) {
			throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(reader);
		} else if (reader.peek() != '^') {
			reader.setCursor(startingCursorPos);
			throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(reader);
		} else {
			reader.skip();
			return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;
		}
	}

	public boolean equals(Object o) {
		if (this == o) return true;

		return o instanceof LookingClientPosArgument lookingPosArgument
				&& this.x == lookingPosArgument.x && this.y == lookingPosArgument.y && this.z == lookingPosArgument.z;
	}

	public int hashCode() {
		return Objects.hash(x, y, z);
	}
}
