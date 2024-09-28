package de.hysky.skyblocker.utils.command.argumenttypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CorpseTypeArgumentType implements ArgumentType<String> {

	private static final List<String> CORPSE_TYPES = List.of("LAPIS", "UMBER", "TUNGSTEN", "VANGUARD");

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		String name = reader.readUnquotedString();
		if (CORPSE_TYPES.contains(name.toUpperCase())) {
			return name.toUpperCase();
		}
		throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return context.getSource() instanceof CommandSource
				? CommandSource.suggestMatching(CORPSE_TYPES.stream().map(String::toLowerCase), builder)
				: Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return CORPSE_TYPES;
	}

	public static CorpseTypeArgumentType corpseType() {
		return new CorpseTypeArgumentType();
	}
}
