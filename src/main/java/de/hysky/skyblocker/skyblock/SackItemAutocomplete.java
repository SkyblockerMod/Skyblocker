package de.hysky.skyblocker.skyblock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.hysky.skyblocker.utils.command.CommandUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import io.github.moulberry.repo.data.NEUItem;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;

public class SackItemAutocomplete {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern BAD_CHARACTERS = Pattern.compile("[☂☘☠✎✧❁❂❈❤⸕]");

	public static @Nullable LiteralCommandNode<FabricClientCommandSource> longCommandNode;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> shortCommandNode;

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(SackItemAutocomplete::loadSackItems);
	}

	private static void loadSackItems() {
		try (InputStream stream = NEURepoManager.file("constants/sacks.json").stream()) {
			JsonObject sacks = JsonParser.parseString(new String(stream.readAllBytes())).getAsJsonObject().getAsJsonObject("sacks");

			Set<String> sackItemIds = sacks.entrySet().stream()
					.map(entry -> entry.getValue().getAsJsonObject())
					.map(sack -> sack.getAsJsonArray("contents"))
					.map(JsonArray::asList)
					.flatMap(List::stream)
					.map(JsonElement::getAsString)
					.collect(Collectors.toUnmodifiableSet());
			Set<String> sackItems = sackItemIds.stream()
					.map(neuId -> {
						NEUItem stack = NEURepoManager.getItemByNeuId(neuId);

						return stack != null ? ChatFormatting.stripFormatting(stack.getDisplayName()) : neuId;
					})
					.map(name -> BAD_CHARACTERS.matcher(name).replaceAll("").trim())
					.collect(Collectors.toUnmodifiableSet());

			longCommandNode = createCommandNode("getfromsacks", sackItems);
			shortCommandNode = createCommandNode("gfs", sackItems);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Sack Item Autocomplete] Failed to load sacks data from the NEU Repo.", e);
		}
	}

	private static LiteralCommandNode<FabricClientCommandSource> createCommandNode(String command, Set<String> sackItems) {
		return literal(command)
				.requires(fccs -> Utils.isOnSkyblock())
				.executes(CommandUtils.noOp)
				.then(argument("item", StringArgumentType.greedyString())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(sackItems, builder))
						.then(argument("amount", IntegerArgumentType.integer(0))) // Adds a nice <amount> text to the suggestion when any number is entered after the item string
						.executes(CommandUtils.noOp)
				)
				.build();
	}
}
