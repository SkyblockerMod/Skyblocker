package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.CommandUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import net.azureaaron.hmapi.data.rank.PackageRank;
import net.azureaaron.hmapi.data.rank.RankType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * the mixin {@link de.hysky.skyblocker.mixins.ClientboundCommandsPacketMixin}
 */
public class WarpAutocomplete {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("warp_autocomplete.json");
	private static final Logger LOGGER = LoggerFactory.getLogger(WarpAutocomplete.class);
	private static final Codec<Object2BooleanMap<String>> MAP_CODEC = CodecUtils.object2BooleanMapCodec(Codec.STRING);

	public static @Nullable LiteralCommandNode<FabricClientCommandSource> commandNode;

	@Init
	public static void init() {
		CompletableFuture.supplyAsync(() -> {
			try {
				String warps = Http.sendGetRequest("https://hysky.de/api/locations");

				return MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(warps)).getOrThrow();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to download warps list", e);
			}
			return Object2BooleanMaps.<String>emptyMap();
		}, Executors.newVirtualThreadPerTaskExecutor()).thenAccept(warps -> {
					if (warps.isEmpty()) {
						getWarpsFromFile();
					} else {
						CompletableFuture.runAsync(() -> {
							Optional<JsonElement> result = MAP_CODEC.encodeStart(JsonOps.INSTANCE, warps).result();
							if (result.isEmpty()) return;
							JsonElement warpsJson = result.get();
							try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
								SkyblockerMod.GSON.toJson(warpsJson, writer);
							} catch (Exception e) {
								LOGGER.error("[Skyblocker] Failed to save warps auto complete", e);
							}
						}, Executors.newVirtualThreadPerTaskExecutor());
						createCommandNode(warps);
					}
				}
		);
	}

	private static void getWarpsFromFile() {
		CompletableFuture.supplyAsync(() -> {
			JsonObject object;
			try (BufferedReader reader = Files.newBufferedReader(FILE)) {
				object = SkyblockerMod.GSON.fromJson(reader, JsonObject.class);
			} catch (NoSuchFileException e) {
				return Object2BooleanMaps.<String>emptyMap();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to read warp autocomplete file", e);
				return Object2BooleanMaps.<String>emptyMap();
			}
			return MAP_CODEC.parse(JsonOps.INSTANCE, object).result().orElse(Object2BooleanMaps.emptyMap());
		}, Executors.newVirtualThreadPerTaskExecutor()).thenAccept(WarpAutocomplete::createCommandNode);
	}

	private static void createCommandNode(Object2BooleanMap<String> warps) {
		commandNode = literal("warp")
				.requires(fabricClientCommandSource -> Utils.isOnSkyblock())
				.executes(CommandUtils.noOp)
				.then(argument("destination", StringArgumentType.greedyString())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(getEligibleWarps(warps), builder))
						.executes(CommandUtils.noOp)
				).build();
	}

	private static Stream<String> getEligibleWarps(Object2BooleanMap<String> warps) {
		return warps.object2BooleanEntrySet().stream()
				.filter(WarpAutocomplete::shouldShowWarp)
				.map(Object2BooleanMap.Entry::getKey);
	}

	private static boolean shouldShowWarp(Object2BooleanMap.Entry<String> entry) {
		return !entry.getBooleanValue() || Utils.isOnBingo() || RankType.compare(Utils.getRank(), PackageRank.MVP_PLUS) >= 0;
	}
}
