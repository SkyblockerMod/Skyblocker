package de.hysky.skyblocker.skyblock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import de.hysky.skyblocker.utils.command.CommandUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

public class RngMeterAutocomplete {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<Map<String, List<String>>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf());
	private static Map<String, List<String>> rngMeters = new HashMap<>();
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> longCommand;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> shortCommand;

	@Init
	public static void init() {
		CompletableFuture.runAsync(() -> {
			try {
				String json = Http.sendGetRequest("https://hysky.de/api/rngmeters");
				rngMeters = CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();

				longCommand = createCommandNode("rngmeter");
				shortCommand = createCommandNode("rng");
			} catch (Exception e) {
				LOGGER.error("[Skyblocker RNG Meter Autocomplete] Failed to load RNG Meter data.", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static LiteralCommandNode<FabricClientCommandSource> createCommandNode(String command) {
		return literal(command)
				.requires(source -> Utils.isOnSkyblock())
				.executes(CommandUtils.noOp)
				.then(argument("type", StringArgumentType.string())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(rngMeters.keySet(), builder))
						.executes(CommandUtils.noOp)
						.then(argument("subtype", StringArgumentType.string())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(rngMeters.getOrDefault(StringArgumentType.getString(context, "type"), List.of()), builder))
								.executes(CommandUtils.noOp)
						)
				).build();
	}
}
