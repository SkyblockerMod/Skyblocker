package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * the mixin {@link de.hysky.skyblocker.mixin.CommandTreeS2CPacketMixin}
 */
public class WarpAutocomplete {

    public static @Nullable LiteralCommandNode<FabricClientCommandSource> COMMAND_THING = null;

    protected static final Logger LOGGER = LoggerFactory.getLogger(WarpAutocomplete.class);

    public static void init() {
        CompletableFuture.supplyAsync(() -> {
            try {
                JsonArray jsonElements = SkyblockerMod.GSON.fromJson(Http.sendGetRequest("https://staging.hysky.de/api/locations"), JsonArray.class);
                return jsonElements.asList().stream().map(JsonElement::getAsString).toList();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to download warps list", e);
            }
            return List.of("");
        }).thenAccept(strings -> COMMAND_THING = ClientCommandManager
                .literal("warp")
                .requires(fabricClientCommandSource -> Utils.isOnSkyblock())
                .then(ClientCommandManager.argument("destination", new ArgType(strings))
                ).build());

    }

    private record ArgType(List<String> possibleWarps) implements ArgumentType<String> {

        @Override
        public String parse(StringReader reader) {
            return reader.readUnquotedString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CommandSource.suggestMatching(possibleWarps, builder);
        }
    }
}
