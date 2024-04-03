package de.hysky.skyblocker.skyblock;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * the mixin {@link de.hysky.skyblocker.mixin.CommandTreeS2CPacketMixin}
 */
public class WarpAutocomplete {

    public static LiteralCommandNode<FabricClientCommandSource> COMMAND_THING;

    public static final String[] WARPS = new String[]{
            "home",
            "island",
            "hub",
            "village",
            "elizabeth",
            "castle",
            "da",
            //"crypt",
            "crypts",
            "museum",
            "dungeons",
            "dhub",
            "barn",
            "desert",
            "trapper",
            "park",
            "jungle",
            "howl",
            "gold",
            "deep",
            "mines",
            "forge",
            "crystals",
            "hollows",
            "ch",
            "nucleus",
            "spider",
            "top",
            "nest",
            "mound",
            "arachne",
            "end",
            "drag",
            "void",
            "sepulture",
            "crimson",
            "nether",
            "isle",
            "kuudra",
            "wasteland",
            "dragontail",
            "scarleton",
            "smoldering",
            "garden",
            "winter",
            "jerry",
            "workshop",
            "basecamp",
            "camp",
            "glacite",
            "tunnels",
            "gt"
    };

    public static void init() {
        COMMAND_THING = ClientCommandManager
                .literal("warp")
                .requires(fabricClientCommandSource -> {
                    boolean onSkyblock = Utils.isOnSkyblock();
                    System.out.println(onSkyblock);
                    return onSkyblock;
                })
                .then(ClientCommandManager.argument("destination", new ArgType(List.of(WARPS)))
                ).build();
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
