package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import net.azureaaron.hmapi.data.rank.PackageRank;
import net.azureaaron.hmapi.data.rank.RankType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * the mixin {@link de.hysky.skyblocker.mixins.CommandTreeS2CPacketMixin}
 */
public class WarpAutocomplete {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarpAutocomplete.class);
    private static final Codec<Object2BooleanMap<String>> MAP_CODEC = CodecUtils.object2BooleanMapCodec(Codec.STRING);

    @Nullable
    public static LiteralCommandNode<FabricClientCommandSource> commandNode;

    public static void init() {
        CompletableFuture.supplyAsync(() -> {
            try {
                String warps = Http.sendGetRequest("https://hysky.de/api/locations");

                return MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(warps)).getOrThrow();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to download warps list", e);
            }
            return Object2BooleanMaps.<String>emptyMap();
        }).thenAccept(warps -> commandNode = literal("warp")
                .requires(fabricClientCommandSource -> Utils.isOnSkyblock())
                .then(argument("destination", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(getEligibleWarps(warps), builder))
                ).build()
        );
    }

    private static Stream<String> getEligibleWarps(Object2BooleanMap<String> warps) {
        return warps.object2BooleanEntrySet().stream()
    	        .filter(WarpAutocomplete::shouldShowWarp)
    	        .map(Object2BooleanMap.Entry::getKey);
    }

    private static boolean shouldShowWarp(Object2BooleanMap.Entry<String> entry) {
        return entry.getBooleanValue() ? RankType.compare(Utils.getRank(), PackageRank.MVP_PLUS) >= 0 : true;
    }
}
