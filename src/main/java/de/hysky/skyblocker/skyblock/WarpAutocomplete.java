package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * the mixin {@link de.hysky.skyblocker.mixins.CommandTreeS2CPacketMixin}
 */
public class WarpAutocomplete {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarpAutocomplete.class);
    @Nullable
    public static LiteralCommandNode<FabricClientCommandSource> commandNode;

    public static void init() {
        CompletableFuture.supplyAsync(() -> {
            try {
                JsonArray jsonElements = SkyblockerMod.GSON.fromJson(Http.sendGetRequest("https://hysky.de/api/locations"), JsonArray.class);
                return jsonElements.asList().stream().map(JsonElement::getAsString).toList();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to download warps list", e);
            }
            return List.<String>of();
        }).thenAccept(warps -> commandNode = literal("warp")
                .requires(fabricClientCommandSource -> Utils.isOnSkyblock())
                .then(argument("destination", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(warps, builder))
                ).build()
        );
    }
}
