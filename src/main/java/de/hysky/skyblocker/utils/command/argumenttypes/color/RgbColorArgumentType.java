package de.hysky.skyblocker.utils.command.argumenttypes.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public final class RgbColorArgumentType implements ArgumentType<Integer> {
	public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.color.rgb.incomplete"));

	@Override
	public Integer parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();
		int redArgument = IntegerArgumentType.integer(0x00, 0xFF).parse(reader);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			int greenArgument = IntegerArgumentType.integer(0x00, 0xFF).parse(reader);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				int blueArgument = IntegerArgumentType.integer(0x00, 0xFF).parse(reader);
				return redArgument << 16 | greenArgument << 8 | blueArgument;
			} else {
				reader.setCursor(i);
				throw INCOMPLETE_EXCEPTION.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw INCOMPLETE_EXCEPTION.createWithContext(reader);
		}
	}

	public static int getInt(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, Integer.class);
	}
}
