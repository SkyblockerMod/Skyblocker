package de.hysky.skyblocker.utils.command.argumenttypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ArgumentWithAlternatives {
	public static final CommandExceptionType TYPE = new CommandExceptionType() {};

	public static <A, B> ArgumentType<Either<A, B>> of(final ArgumentType<A> a, final ArgumentType<B> b) {
		return new EitherArgumentType<>(a, b);
	}

	public static <A, B> ArgumentType<A>  of(final ArgumentType<A> a, final ArgumentType<B> b, Function<B, A> converter) {
		EitherArgumentType<A, B> type = new EitherArgumentType<>(a, b);
		return new ArgumentType<>() {
			@Override
			public A parse(StringReader reader) throws CommandSyntaxException {
				return type.parse(reader).map(Function.identity(), converter);
			}

			@Override
			public <S> A parse(StringReader reader, S source) throws CommandSyntaxException {
				return type.parse(reader).map(Function.identity(), converter);
			}

			@Override
			public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
				return type.listSuggestions(context, builder);
			}

			@Override
			public Collection<String> getExamples() {
				return type.getExamples();
			}
		};
	}

	private record EitherArgumentType<A, B>(ArgumentType<A> a, ArgumentType<B> b) implements ArgumentType<Either<A, B>> {

		@Override
		public Either<A, B> parse(StringReader reader) throws CommandSyntaxException {
			CommandSyntaxException first;
			int cursor = reader.getCursor();
			try {
				A parse = a.parse(reader);
				System.out.println("Got A: " + parse);
				return Either.left(parse);
			} catch (CommandSyntaxException e) {
				first = e;
			}
			reader.setCursor(cursor);
			try {
				B parse = b.parse(reader);
				System.out.println("Got B: " + parse);
				return Either.right(parse);
			} catch (CommandSyntaxException e) {
				throw new CommandSyntaxException(TYPE, () -> first.getMessage() + " or " + first.getMessage());
			}
		}

		@Override
		public <S> Either<A, B> parse(StringReader reader, S source) throws CommandSyntaxException {
			CommandSyntaxException first;
			int cursor = reader.getCursor();
			try {
				return Either.left(a.parse(reader, source));
			} catch (CommandSyntaxException e) {
				first = e;
			}
			reader.setCursor(cursor);
			try {
				return Either.right(b.parse(reader, source));
			} catch (CommandSyntaxException e) {
				throw new CommandSyntaxException(TYPE, () -> first.getMessage() + " or " + first.getMessage());
			}
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			CompletableFuture<Suggestions> first = a.listSuggestions(context, builder);
			CompletableFuture<Suggestions> second = b.listSuggestions(context, builder);
			final CompletableFuture<Suggestions> result = new CompletableFuture<>();
			CompletableFuture.allOf(first, second).thenRun(() -> result.complete(Suggestions.merge(context.getInput(), List.of(first.join(), second.join()))));
			return result;
		}

		@Override
		public Collection<String> getExamples() {
			return Stream.concat(a.getExamples().stream(), a.getExamples().stream()).toList();
		}
	}
}
