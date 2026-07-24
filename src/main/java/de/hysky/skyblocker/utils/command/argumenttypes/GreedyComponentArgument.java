package de.hysky.skyblocker.utils.command.argumenttypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class GreedyComponentArgument implements ArgumentType<Component> {

	private final ComponentArgument delegate;

	public GreedyComponentArgument(ComponentArgument delegate) {
		this.delegate = delegate;
	}

	@Override
	public Component parse(StringReader reader) throws CommandSyntaxException {
		if (reader.getRemaining().indexOf(' ') >= 0 && reader.peek() != '"' && reader.peek() != '\'') {
			MutableComponent literal = Component.literal(reader.getRemaining());
			reader.setCursor(reader.getTotalLength());
			return literal;
		}
		return delegate.parse(reader);
	}

	@Override
	public <S> Component parse(StringReader reader, S source) throws CommandSyntaxException {
		if (reader.getRemaining().indexOf(' ') >= 0 && reader.peek() != '"' && reader.peek() != '\'') {
			MutableComponent literal = Component.literal(reader.getRemaining());
			reader.setCursor(reader.getTotalLength());
			return literal;
		}
		return delegate.parse(reader, source);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return delegate.listSuggestions(context, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return delegate.getExamples();
	}
}
