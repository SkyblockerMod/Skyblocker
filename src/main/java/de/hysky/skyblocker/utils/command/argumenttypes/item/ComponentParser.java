package de.hysky.skyblocker.utils.command.argumenttypes.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ComponentParser extends ItemParser {
	public ComponentParser(HolderLookup.Provider registries) {
		super(registries);
	}

	@Override
	public ItemInput parse(StringReader reader) throws CommandSyntaxException {
		final DataComponentPatch.Builder componentsBuilder = DataComponentPatch.builder();
		this.parse(reader, new ItemParser.Visitor() /* ItemParser$1 */ {
			@Override
			public <T> void visitComponent(final DataComponentType<T> type, final T value) {
				componentsBuilder.set(type, value);
			}

			@Override
			public <T> void visitRemovedComponent(final DataComponentType<T> type) {
				componentsBuilder.remove(type);
			}
		});
		DataComponentPatch components = componentsBuilder.build();
		return new ItemInput(Holder.direct(Items.AIR), components);
	}

	@Override
	public void parse(StringReader reader, Visitor visitor) throws CommandSyntaxException {
		int cursor = reader.getCursor();

		try {
			(new ComponentState(reader, visitor)).parse();
		} catch (CommandSyntaxException e) {
			reader.setCursor(cursor);
			throw e;
		}
	}

	@Override
	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder builder) {
		StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());
		SuggestionsVisitor handler = new ItemParser.SuggestionsVisitor();
		ItemParser.State state = new ComponentState(reader, handler);

		try {
			state.parse();
		} catch (CommandSyntaxException _) {
		}

		return handler.resolveSuggestions(builder, reader);
	}


	private class ComponentState extends State {

		protected ComponentState(StringReader reader, Visitor visitor) {
			super(reader, visitor);
		}

		@Override
		protected void readItem() {}

		@Override
		protected CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder builder) {
			return Suggestions.empty();
		}
	}
}
