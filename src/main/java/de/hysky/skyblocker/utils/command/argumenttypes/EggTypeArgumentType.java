package de.hysky.skyblocker.utils.command.argumenttypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.hysky.skyblocker.skyblock.chocolatefactory.EggFinder;
import net.minecraft.command.CommandSource;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class EggTypeArgumentType implements ArgumentType<EggFinder.EggType> {
	@Override
	public EggFinder.EggType parse(StringReader reader) throws CommandSyntaxException {
		String name = reader.readUnquotedString();
		for (EggFinder.EggType type : EggFinder.EggType.entries) {
			if (type.name().equalsIgnoreCase(name)) return type;
		}
		throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return context.getSource() instanceof CommandSource
			? CommandSource.suggestMatching(EggFinder.EggType.entries.stream().map(EggFinder.EggType::name).map(String::toLowerCase), builder)
			: Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return EggFinder.EggType.entries.stream().map(EggFinder.EggType::name).map(String::toLowerCase).toList();
	}

	public static EggTypeArgumentType eggType() {
		return new EggTypeArgumentType();
	}
}
