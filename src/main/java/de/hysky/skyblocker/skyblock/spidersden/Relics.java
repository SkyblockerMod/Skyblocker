package de.hysky.skyblocker.skyblock.spidersden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Relics {
    private static final Logger LOGGER = LoggerFactory.getLogger(Relics.class);
    private static CompletableFuture<Void> relicsLoaded;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static int totalRelics = 0;
    private static final List<BlockPos> relics = new ArrayList<>();
    private static final Map<String, Set<BlockPos>> foundRelics = new HashMap<>();

    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(Relics::loadRelics);
        ClientLifecycleEvents.CLIENT_STOPPING.register(Relics::saveFoundRelics);
        ClientCommandRegistrationCallback.EVENT.register(Relics::registerCommands);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Relics::render);
        ClientReceiveMessageEvents.GAME.register(Relics::onChatMessage);
    }

    private static void loadRelics(MinecraftClient client) {
        relicsLoaded = CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = client.getResourceManager().openAsReader(new Identifier(SkyblockerMod.NAMESPACE, "spidersden/relics.json"))) {
                for (Map.Entry<String, JsonElement> json : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    if (json.getKey().equals("total")) {
                        totalRelics = json.getValue().getAsInt();
                    } else if (json.getKey().equals("locations")) {
                        for (JsonElement locationJson : json.getValue().getAsJsonArray().asList()) {
                            JsonObject posData = locationJson.getAsJsonObject();
                            relics.add(new BlockPos(posData.get("x").getAsInt(), posData.get("y").getAsInt(), posData.get("z").getAsInt()));
                        }
                    }
                }
                LOGGER.info("[Skyblocker] Loaded relics locations");
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load relics locations", e);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(SkyblockerMod.CONFIG_DIR.resolve("found_relics.json").toFile()))) {
                for (Map.Entry<String, JsonElement> profileJson : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    Set<BlockPos> foundRelicsForProfile = new HashSet<>();
                    for (JsonElement foundRelicsJson : profileJson.getValue().getAsJsonArray().asList()) {
                        foundRelicsForProfile.add(PosUtils.parsePosString(foundRelicsJson.getAsString()));
                    }
                    foundRelics.put(profileJson.getKey(), foundRelicsForProfile);
                }
                LOGGER.debug("[Skyblocker] Loaded found relics");
            } catch (FileNotFoundException ignored) {
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load found relics", e);
            }
        });
    }

    private static void saveFoundRelics(MinecraftClient client) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SkyblockerMod.CONFIG_DIR.resolve("found_relics.json").toFile()))) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, Set<BlockPos>> foundRelicsForProfile : foundRelics.entrySet()) {
                JsonArray foundRelicsJson = new JsonArray();
                for (BlockPos foundRelic : foundRelicsForProfile.getValue()) {
                    foundRelicsJson.add(PosUtils.getPosString(foundRelic));
                }
                json.add(foundRelicsForProfile.getKey(), foundRelicsJson);
            }
            SkyblockerMod.GSON.toJson(json, writer);
            LOGGER.debug("[Skyblocker] Saved found relics");
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to write found relics to file", e);
        }
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("relics")
                        .then(literal("markAllFound").executes(context -> {
                            Relics.markAllFound();
                            context.getSource().sendFeedback(Text.translatable("skyblocker.relics.markAllFound"));
                            return 1;
                        }))
                        .then(literal("markAllMissing").executes(context -> {
                            Relics.markAllMissing();
                            context.getSource().sendFeedback(Text.translatable("skyblocker.relics.markAllMissing"));
                            return 1;
                        }))));
    }

    private static void render(WorldRenderContext context) {
        SkyblockerConfig.Relics config = SkyblockerConfigManager.get().locations.spidersDen.relics;

        if (config.enableRelicsHelper && relicsLoaded.isDone() && Utils.getLocationRaw().equals("combat_1")) {
            for (BlockPos fairySoulPos : relics) {
                boolean isRelicMissing = isRelicMissing(fairySoulPos);
                if (!isRelicMissing && !config.highlightFoundRelics) continue;
                float[] colorComponents = isRelicMissing ? DyeColor.YELLOW.getColorComponents() : DyeColor.BROWN.getColorComponents();
                RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, fairySoulPos, colorComponents, 0.5F);
            }
        }
    }

    private static void onChatMessage(Text text, boolean overlay) {
        String message = text.getString();
        if (message.equals("You've already found this relic!") || message.startsWith("+10,000 Coins! (") && message.endsWith("/28 Relics)")) {
            markClosestRelicFound();
        }
    }

    private static void markClosestRelicFound() {
        if (!relicsLoaded.isDone()) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            LOGGER.warn("[Skyblocker] Failed to mark closest relic as found because player is null");
            return;
        }
        relics.stream()
                .filter(Relics::isRelicMissing)
                .min(Comparator.comparingDouble(relicPos -> relicPos.getSquaredDistance(player.getPos())))
                .filter(relicPos -> relicPos.getSquaredDistance(player.getPos()) <= 16)
                .ifPresent(relicPos -> {
                    foundRelics.computeIfAbsent(Utils.getProfile(), profileKey -> new HashSet<>());
                    foundRelics.get(Utils.getProfile()).add(relicPos);
                });
    }

    private static boolean isRelicMissing(BlockPos relicPos) {
        Set<BlockPos> foundRelicsForProfile = foundRelics.get(Utils.getProfile());
        return foundRelicsForProfile == null || !foundRelicsForProfile.contains(relicPos);
    }

    private static void markAllFound() {
        foundRelics.computeIfAbsent(Utils.getProfile(), profileKey -> new HashSet<>());
        foundRelics.get(Utils.getProfile()).addAll(relics);
    }

    private static void markAllMissing() {
        Set<BlockPos> foundRelicsForProfile = foundRelics.get(Utils.getProfile());
        if (foundRelicsForProfile != null) {
            foundRelicsForProfile.clear();
        }
    }
}
