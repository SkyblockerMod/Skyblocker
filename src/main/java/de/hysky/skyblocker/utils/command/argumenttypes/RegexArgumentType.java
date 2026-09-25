package de.hysky.skyblocker.utils.command.argumenttypes;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * Greedy.
 */
public class RegexArgumentType implements ArgumentType<Pattern> {
	public static final CommandExceptionType EXCEPTION_TYPE = new CommandExceptionType() {};
	@Override
	public Pattern parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		try {
			Pattern compile = Pattern.compile(reader.getRemaining());
			reader.setCursor(reader.getTotalLength());
			return compile;
		} catch (PatternSyntaxException e) {
			reader.setCursor(cursor);
			throw new CommandSyntaxException(EXCEPTION_TYPE, e::getDescription, reader.getRemaining(), e.getIndex());
		}
	}
}
