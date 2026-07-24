package de.hysky.skyblocker.utils.command.argumenttypes.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponentPatch;

import java.util.concurrent.CompletableFuture;

public class ComponentArgument implements ArgumentType<DataComponentPatch> {

	private final ComponentParser parser;

	public ComponentArgument(CommandBuildContext context) {
		parser = new ComponentParser(context);
	}

	@Override
	public DataComponentPatch parse(StringReader reader) throws CommandSyntaxException {
		return parser.parse(reader).components();
		// insert a fake item so we directly go to the component part of the ItemParser
		/*final String fakeItem = "minecraft:stone";
		StringReader fakeReader = new StringReader(new StringBuilder(reader.getString()).insert(reader.getCursor(), fakeItem).toString());
		fakeReader.setCursor(reader.getCursor());
		DataComponentPatch components = this.parser.parse(fakeReader).components();
		reader.setCursor(fakeReader.getCursor() - fakeItem.length());
		return components;*/
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		/*StringBuilder stringBuilder = new StringBuilder(builder.getInput());
		stringBuilder.insert(builder.getStart(), "minecraft:stone");
		SuggestionsBuilder fakeBuilder = new SuggestionsBuilder(stringBuilder.toString(), builder.getStart());
		System.out.println("Original: " + builder.getInput() + " " + builder.getStart() + " " + builder.getRemaining());
		System.out.println("Fake: " + fakeBuilder.getInput() + " " + fakeBuilder.getStart() + " " + fakeBuilder.getRemaining());
		return this.parser.fillSuggestions(fakeBuilder);*/
		return parser.fillSuggestions(builder);
	}
}
