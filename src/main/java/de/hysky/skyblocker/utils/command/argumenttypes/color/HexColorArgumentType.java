package de.hysky.skyblocker.utils.command.argumenttypes.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("RedundantCast")
public final class HexColorArgumentType implements ArgumentType<Integer> {
	public static final DynamicCommandExceptionType WRONG_INPUT_WIDTH = new DynamicCommandExceptionType(found -> Component.translatable("argument.color.hex.invalidString", ((String) found).length()));
	public static final DynamicCommandExceptionType INVALID_CHARACTER = new DynamicCommandExceptionType(character -> Component.translatable("argument.color.hex.invalidChar", (String) character));

	@Override
	public Integer parse(StringReader reader) throws CommandSyntaxException {
		String input = reader.readString();
		if (StringUtils.startsWithIgnoreCase(input, "0x")) input = input.substring(2);
//		else if (input.startsWith("#")) input = input.substring(1); // This doesn't work because minecraft has the # prefix reserved for tags, so inputs with that prefix never reach this reader

		if (input.length() != 6) throw WRONG_INPUT_WIDTH.create(input);

		for (int i = 0; i < input.length(); i++) {
			char character = input.charAt(i);
			if ((character < '0' || character > '9') && (character < 'a' || character > 'f') && (character < 'A' || character > 'F')) {
				throw INVALID_CHARACTER.create(String.valueOf(character)); //Have to wrap character in a string, because mcdev doesn't appreciate chars and I cba to suppress the warnings
			}
		}

		return Integer.decode("#" + input);
	}

	public static int getInt(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, Integer.class);
	}
}
