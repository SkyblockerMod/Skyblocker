package de.hysky.skyblocker.utils.command.argumenttypes.color;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 * Utility class that provides static methods for abstracting away the actual argument type classes.
 */
public final class ColorArgumentType {
	private ColorArgumentType() {} // Prevent instantiation

	public static RgbColorArgumentType rgb() {
		return new RgbColorArgumentType();
	}

	public static HexColorArgumentType hex() {
		return new HexColorArgumentType();
	}

	public static int getIntFromHex(CommandContext<FabricClientCommandSource> context, String name) {
		return HexColorArgumentType.getInt(context, name);
	}

	public static int getIntFromRgb(CommandContext<FabricClientCommandSource> context, String name) {
		return RgbColorArgumentType.getInt(context, name);
	}
}
