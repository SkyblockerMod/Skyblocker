package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * the mixin {@link de.hysky.skyblocker.mixins.CommandTreeS2CPacketMixin}
 */
public class JoinInstanceAutocomplete {
	private static final Logger LOGGER = LoggerFactory.getLogger(JoinInstanceAutocomplete.class);

	@Nullable
	public static LiteralCommandNode<FabricClientCommandSource> joinInstanceCommand;
	@Nullable
	public static LiteralCommandNode<FabricClientCommandSource> dungeonCommand;
	@Nullable
	public static LiteralCommandNode<FabricClientCommandSource> kuudraCommand;

	private static Map<String, String> instanceMap;

	@Init
	public static void init() {
		CompletableFuture.runAsync(() -> {
			try {
				String json = Http.sendGetRequest("https://hysky.de/api/joininstances");

				JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
				instanceMap = obj.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));

				joinInstanceCommand = buildCommand("joininstance", s -> true);
				dungeonCommand = buildCommand("joindungeon", s -> instanceMap.get(s).equalsIgnoreCase("Catacombs"));
				kuudraCommand = buildCommand("joinkuudra", s -> instanceMap.get(s).equalsIgnoreCase("Kuudra"));

			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to load joininstance list", e);
			}
		});
	}

	private static LiteralCommandNode<FabricClientCommandSource> buildCommand(String command, java.util.function.Predicate<String> filter) {
		return literal(command)
				.requires(source -> Utils.isOnSkyblock())
				.then(argument("instance", StringArgumentType.word())
						.suggests((context, builder) -> CommandSource.suggestMatching(
								instanceMap.keySet().stream().filter(filter).sorted(),
								builder)))
				.build();
	}
}
