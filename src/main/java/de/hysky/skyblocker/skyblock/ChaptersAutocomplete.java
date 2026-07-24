package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class ChaptersAutocomplete {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static List<String> locations = List.of();
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> singularCommandNode;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> pluralCommandNode;

	@Init
	public static void init() {
		CompletableFuture.runAsync(() -> {
			try {
				String json = Http.sendGetRequest("https://hysky.de/api/chapters");
				ChaptersData data = ChaptersData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
				locations = data.locations;
				singularCommandNode = createCommandNode("chapter");
				pluralCommandNode = createCommandNode("chapters");
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Chapters Autocomplete] Failed to load Chapters data...", e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);
	}

	private static LiteralCommandNode<FabricClientCommandSource> createCommandNode(String command) {
		return literal(command)
				.requires(_ -> Utils.isOnSkyblock())
				.executes(CommandUtils.noOp)
				.then(argument("island", StringArgumentType.string())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(locations, builder))
						.executes(CommandUtils.noOp)
				).build();
	}

	private record ChaptersData(List<String> locations) {
		private static final Codec<ChaptersData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().fieldOf("locations").forGetter(ChaptersData::locations)
		).apply(instance, ChaptersData::new));
	}
}
