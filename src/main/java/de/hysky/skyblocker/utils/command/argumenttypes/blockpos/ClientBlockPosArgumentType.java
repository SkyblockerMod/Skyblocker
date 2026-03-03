package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_OUT_OF_BOUNDS;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_OUT_OF_WORLD;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_NOT_LOADED;

// Uses the static fields of BlockPosArgument to not create the same field twice
public class ClientBlockPosArgumentType implements ArgumentType<ClientPosArgument> {
	public static ClientBlockPosArgumentType blockPos() {
		return new ClientBlockPosArgumentType();
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		return getLoadedBlockPos(context, context.getSource().getWorld(), name);
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, ClientLevel world, String name) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(context, name);
		//FIXME Vanilla still uses this deprecated method, watch out in future updates in case this changes
		if (!world.hasChunkAt(blockPos)) throw ERROR_NOT_LOADED.create();
		if (!world.isInWorldBounds(blockPos)) throw ERROR_OUT_OF_WORLD.create();

		return blockPos;
	}

	public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
	}

	public static BlockPos getValidBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(context, name);
		if (!Level.isInSpawnableBounds(blockPos)) {
			throw ERROR_OUT_OF_BOUNDS.create();
		} else {
			return blockPos;
		}
	}

	public ClientPosArgument parse(StringReader stringReader) throws CommandSyntaxException {
		return stringReader.canRead() && stringReader.peek() == '^' ? LookingClientPosArgument.parse(stringReader) : DefaultClientPosArgument.parse(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof SharedSuggestionProvider commandSource)) return Suggestions.empty();

		String string = builder.getRemaining();
		Collection<SharedSuggestionProvider.TextCoordinates> collection = !string.isEmpty() && string.charAt(0) == '^' ? Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL) : commandSource.getRelevantCoordinates();

		return SharedSuggestionProvider.suggestCoordinates(string, collection, builder, Commands.createValidator(this::parse));
	}

	@Override
	public Collection<String> getExamples() {
		return BlockPosArgument.EXAMPLES;
	}
}
