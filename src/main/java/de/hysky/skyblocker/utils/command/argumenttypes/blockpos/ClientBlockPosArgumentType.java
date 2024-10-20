package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.argument.BlockPosArgumentType.*;

// Uses the static fields of BlockPosArgumentType to not create the same field twice
public class ClientBlockPosArgumentType implements ArgumentType<ClientPosArgument> {
	public static ClientBlockPosArgumentType blockPos() {
		return new ClientBlockPosArgumentType();
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		return getLoadedBlockPos(context, context.getSource().getWorld(), name);
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, ClientWorld world, String name) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(context, name);
		//FIXME Vanilla still uses this deprecated method, watch out in future updates in case this changes
		if (!world.isChunkLoaded(blockPos)) throw UNLOADED_EXCEPTION.create();
		if (!world.isInBuildLimit(blockPos)) throw OUT_OF_WORLD_EXCEPTION.create();

		return blockPos;
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
		if (!(context.getSource() instanceof CommandSource commandSource)) return Suggestions.empty();

		String string = builder.getRemaining();
		Collection<CommandSource.RelativePosition> collection = !string.isEmpty() && string.charAt(0) == '^' ? Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL) : commandSource.getBlockPositionSuggestions();

		return CommandSource.suggestPositions(string, collection, builder, CommandManager.getCommandValidator(this::parse));
	}

	@Override
	public Collection<String> getExamples() {
		return BlockPosArgumentType.EXAMPLES;
	}
}
