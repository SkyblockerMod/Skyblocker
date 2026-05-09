package de.hysky.skyblocker.skyblock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AuctionBazaarAutocomplete {
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> ahsNode;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> ahsearchNode;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> bzNode;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> bazaarNode;

	@Init(priority = 100) // Load after SearchOverManager
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(AuctionBazaarAutocomplete::createNodes);
	}

	// since /bzs is only client-side it needs separate handling
	public static CompletableFuture<Suggestions> suggestBzs(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(SearchOverManager.getBazaarItems(), builder);
	}

	private static void createNodes() {
		ahsNode = createNode("ahs", SearchOverManager::getAuctionItems);
		ahsearchNode = createNode("ahsearch", SearchOverManager::getAuctionItems);
		bzNode = createNode("bz", SearchOverManager::getBazaarItems);
		bazaarNode = createNode("bazaar", SearchOverManager::getBazaarItems);
	}

	private static LiteralCommandNode<FabricClientCommandSource> createNode(String command, Supplier<Iterable<String>> suggester) {
		return literal(command)
				.requires(_ -> Utils.isOnSkyblock())
				.executes(CommandUtils.noOp)
				.then(argument("item", StringArgumentType.greedyString())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(suggester.get(), builder))
						.executes(CommandUtils.noOp))
				.build();
	}
}
