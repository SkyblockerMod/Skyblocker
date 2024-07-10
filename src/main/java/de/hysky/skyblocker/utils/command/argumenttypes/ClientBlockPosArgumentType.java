package de.hysky.skyblocker.utils.command.argumenttypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ClientBlockPosArgumentType implements ArgumentType<ClientPosArgument> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
	public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.unloaded"));
	public static final SimpleCommandExceptionType OUT_OF_WORLD_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.outofworld"));
	public static final SimpleCommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.outofbounds"));

	public static ClientBlockPosArgumentType blockPos() {
		return new ClientBlockPosArgumentType();
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		ClientWorld clientWorld = context.getSource().getWorld();
		return getLoadedBlockPos(context, clientWorld, name);
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, ClientWorld world, String name) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(context, name);
		if (!world.isChunkLoaded(blockPos)) {
			throw UNLOADED_EXCEPTION.create();
		} else if (!world.isInBuildLimit(blockPos)) {
			throw OUT_OF_WORLD_EXCEPTION.create();
		} else {
			return blockPos;
		}
	}

	public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
	}

	public static BlockPos getValidBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(context, name);
		if (!World.isValid(blockPos)) {
			throw OUT_OF_BOUNDS_EXCEPTION.create();
		} else {
			return blockPos;
		}
	}

	public ClientPosArgument parse(StringReader stringReader) throws CommandSyntaxException {
		return stringReader.canRead() && stringReader.peek() == '^' ? LookingClientPosArgument.parse(stringReader) : DefaultClientPosArgument.parse(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof CommandSource)) {
			return Suggestions.empty();
		} else {
			String string = builder.getRemaining();
			Collection<CommandSource.RelativePosition> collection;
			if (!string.isEmpty() && string.charAt(0) == '^') {
				collection = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
			} else {
				collection = ((CommandSource)context.getSource()).getBlockPositionSuggestions();
			}

			return CommandSource.suggestPositions(string, collection, builder, CommandManager.getCommandValidator(this::parse));
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
