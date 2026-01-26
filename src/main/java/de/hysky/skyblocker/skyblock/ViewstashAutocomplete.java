package de.hysky.skyblocker.skyblock;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ViewstashAutocomplete {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		return literal("viewstash")
					.requires(fabricClientCommandSource -> Utils.isOnSkyblock())
					.executes(CommandUtils.noOp)
					.then(argument("stash", StringArgumentType.word())
									.suggests((context, builder) -> SharedSuggestionProvider.suggest(Stream.of("material", "item"), builder))
									.executes(CommandUtils.noOp)
					).build();
	}
}
