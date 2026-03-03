package de.hysky.skyblocker.skyblock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import de.hysky.skyblocker.utils.command.CommandUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

public class CallAutocomplete {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> commandNode;

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(CallAutocomplete::loadCallNames);
	}

	private static void loadCallNames() {
		try (InputStream stream = NEURepoManager.file("constants/abiphone.json").stream()) {
			String data = new String(stream.readAllBytes());
			Map<String, AbiphoneContact> contacts = AbiphoneContact.MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(data)).getOrThrow();
			List<String> suggestions = contacts.entrySet().stream()
					.map(entry -> computeCallNames(entry.getKey(), entry.getValue()))
					.flatMap(List::stream)
					.toList();

			commandNode = literal("call")
					.requires(fccs -> Utils.isOnSkyblock())
					.executes(CommandUtils.noOp)
					.then(argument("contact", StringArgumentType.greedyString())
							.suggests((context, builder) -> SharedSuggestionProvider.suggest(suggestions, builder))
							.executes(CommandUtils.noOp))
					.build();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Call Autocomplete] Failed to load abiphone contacts list!", e);
		}
	}

	private static List<String> computeCallNames(String npcName, AbiphoneContact contact) {
		return contact.callNames().isPresent() ? contact.callNames().get() : List.of(formatDefaultCallName(npcName));
	}

	private static String formatDefaultCallName(String npcName) {
		return npcName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "");
	}

	private record AbiphoneContact(Optional<List<String>> callNames) {
		private static final Codec<AbiphoneContact> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().optionalFieldOf("callNames").forGetter(AbiphoneContact::callNames)
				).apply(instance, AbiphoneContact::new));
		private static final Codec<Map<String, AbiphoneContact>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
	}
}
