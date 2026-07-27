package de.hysky.skyblocker.skyblock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hysky.skyblocker.utils.command.CommandUtils;
import io.github.moulberry.repo.constants.AbiphoneContact;

import org.jspecify.annotations.Nullable;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

public class CallAutocomplete {
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> commandNode;

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(CallAutocomplete::loadCallNames);
	}

	private static void loadCallNames() {
		List<String> suggestions = new ArrayList<>();

		for (Map.Entry<String, AbiphoneContact> entry : NEURepoManager.getConstants().getAbiphoneContacts().entrySet()) {
			String npcName = entry.getKey();
			List<String> callNameOverrides = entry.getValue().getCallNames();

			suggestions.addAll(computeCallNames(npcName, callNameOverrides));
		}

		commandNode = literal("call")
				.requires(_ -> Utils.isOnSkyblock())
				.executes(CommandUtils.noOp)
				.then(argument("contact", StringArgumentType.greedyString())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(suggestions, builder))
						.executes(CommandUtils.noOp))
				.build();
	}

	private static List<String> computeCallNames(String npcName, @Nullable List<String> callNameOverrides) {
		return callNameOverrides != null ? callNameOverrides : List.of(formatDefaultCallName(npcName));
	}

	private static String formatDefaultCallName(String npcName) {
		return npcName.toLowerCase(Locale.ENGLISH);
	}
}
