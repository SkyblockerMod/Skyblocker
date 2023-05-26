package me.xmrvizzy.skyblocker.skyblock;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.NEURepo;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FairySouls {
    private static final Logger LOGGER = LoggerFactory.getLogger(FairySouls.class);
    private static CompletableFuture<Void> fairySoulsLoaded;
    private static final Map<String, Set<BlockPos>> fairySouls = new HashMap<>();
    private static final Map<String, Map<String, Set<BlockPos>>> foundFairies = new HashMap<>();

    public static void init() {
        fairySoulsLoaded = NEURepo.runAsyncAfterLoad(() -> {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(NEURepo.LOCAL_REPO_DIR.resolve("constants").resolve("fairy_souls.json").toFile()));
                for (Map.Entry<String, JsonElement> fairySoulJson : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    if (fairySoulJson.getKey().equals("//") || fairySoulJson.getKey().equals("Max Souls")) {
                        continue;
                    }
                    ImmutableSet.Builder<BlockPos> fairySoulsForLocation = ImmutableSet.builder();
                    for (JsonElement fairySoul : fairySoulJson.getValue().getAsJsonArray().asList()) {
                        fairySoulsForLocation.add(parseBlockPos(fairySoul));
                    }
                    fairySouls.put(fairySoulJson.getKey(), fairySoulsForLocation.build());
                }
                reader = new BufferedReader(new FileReader(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json").toFile()));
                for (Map.Entry<String, JsonElement> foundFairiesForProfileJson : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
                    Map<String, Set<BlockPos>> foundFairiesForProfile = new HashMap<>();
                    for (Map.Entry<String, JsonElement> foundFairiesForLocationJson : foundFairiesForProfileJson.getValue().getAsJsonObject().asMap().entrySet()) {
                        Set<BlockPos> foundFairiesForLocation = new HashSet<>();
                        for (JsonElement foundFairy : foundFairiesForLocationJson.getValue().getAsJsonArray().asList()) {
                            foundFairiesForLocation.add(parseBlockPos(foundFairy));
                        }
                        foundFairiesForProfile.put(foundFairiesForLocationJson.getKey(), foundFairiesForLocation);
                    }
                    foundFairies.put(foundFairiesForProfileJson.getKey(), foundFairiesForProfile);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("Failed to load found fairy souls.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(FairySouls::saveFoundFairySouls);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(FairySouls::render);
        ClientReceiveMessageEvents.GAME.register(FairySouls::onChatMessage);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("fairySouls")
                        .then(literal("markAllInCurrentIslandFound").executes(context -> {
                            FairySouls.markAllFairiesFound();
                            context.getSource().sendFeedback(Text.translatable("skyblocker.fairySouls.markAllFound"));
                            return 1;
                        }))
                        .then(literal("markAllInCurrentIslandMissing").executes(context -> {
                            FairySouls.markAllFairiesNotFound();
                            context.getSource().sendFeedback(Text.translatable("skyblocker.fairySouls.markAllMissing"));
                            return 1;
                        })))));
    }

    private static BlockPos parseBlockPos(JsonElement posJson) {
        String[] posArray = posJson.getAsString().split(",");
        return new BlockPos(Integer.parseInt(posArray[0]), Integer.parseInt(posArray[1]), Integer.parseInt(posArray[2]));
    }

    public static void saveFoundFairySouls(MinecraftClient client) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(SkyblockerMod.CONFIG_DIR.resolve("found_fairy_souls.json").toFile()));
            JsonObject foundFairiesJson = new JsonObject();
            for (Map.Entry<String, Map<String, Set<BlockPos>>> foundFairiesForProfile : foundFairies.entrySet()) {
                JsonObject foundFairiesForProfileJson = new JsonObject();
                for (Map.Entry<String, Set<BlockPos>> foundFairiesForLocation : foundFairiesForProfile.getValue().entrySet()) {
                    JsonArray foundFairiesForLocationJson = new JsonArray();
                    for (BlockPos foundFairy : foundFairiesForLocation.getValue()) {
                        foundFairiesForLocationJson.add(foundFairy.getX() + "," + foundFairy.getY() + "," + foundFairy.getZ());
                    }
                    foundFairiesForProfileJson.add(foundFairiesForLocation.getKey(), foundFairiesForLocationJson);
                }
                foundFairiesJson.add(foundFairiesForProfile.getKey(), foundFairiesForProfileJson);
            }
            SkyblockerMod.GSON.toJson(foundFairiesJson, writer);
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Failed to write found fairy souls to file.");
        }
    }

    public static void render(WorldRenderContext context) {
        if (!SkyblockerConfig.get().general.fairySouls.enableFairySoulsHelper) {
            return;
        }
        if (!fairySoulsLoaded.isDone()) {
            LOGGER.warn("Fairy souls are not loaded yet.");
            return;
        }
        if (!fairySouls.containsKey(Utils.getLocationRaw())) {
            return;
        }
        for (BlockPos fairySoul : fairySouls.get(Utils.getLocationRaw())) {
            float[] colorComponents = isFairySoulNotFound(fairySoul) ? DyeColor.GREEN.getColorComponents() : DyeColor.RED.getColorComponents();
            RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, fairySoul, colorComponents, 0.5F);
        }
    }

    private static boolean isFairySoulNotFound(BlockPos fairySoul) {
        Map<String, Set<BlockPos>> foundFairiesForProfile = foundFairies.get(Utils.getProfile());
        if (foundFairiesForProfile == null) {
            return true;
        }
        Set<BlockPos> foundFairiesForProfileAndLocation = foundFairiesForProfile.get(Utils.getLocationRaw());
        if (foundFairiesForProfileAndLocation == null) {
            return true;
        }
        return !foundFairiesForProfileAndLocation.contains(fairySoul);
    }

    public static void onChatMessage(Text text, boolean overlay) {
        String message = text.getString();
        if (message.equals("You have already found that Fairy Soul!") || message.equals("SOUL! You found a Fairy Soul!")) {
            markClosestFairyFound();
        }
    }

    private static void markClosestFairyFound() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            LOGGER.warn("Failed to mark closest fairy soul as found because player is null.");
            return;
        }
        fairySouls.get(Utils.getLocationRaw()).stream().filter(FairySouls::isFairySoulNotFound).min(Comparator.comparingDouble(fairySoul -> fairySoul.getSquaredDistance(player.getPos()))).ifPresent(fairySoul -> {
            initializeFoundFairiesForCurrentProfileAndLocation();
            foundFairies.get(Utils.getProfile()).get(Utils.getLocationRaw()).add(fairySoul);
        });
    }

    public static void markAllFairiesFound() {
        initializeFoundFairiesForCurrentProfileAndLocation();
        foundFairies.get(Utils.getProfile()).get(Utils.getLocationRaw()).addAll(fairySouls.get(Utils.getLocationRaw()));
    }

    public static void markAllFairiesNotFound() {
        Map<String, Set<BlockPos>> foundFairiesForProfile = foundFairies.get(Utils.getProfile());
        if (foundFairiesForProfile != null) {
            foundFairiesForProfile.remove(Utils.getLocationRaw());
        }
    }

    private static void initializeFoundFairiesForCurrentProfileAndLocation() {
        initializeFoundFairiesForProfileAndLocation(Utils.getProfile(), Utils.getLocationRaw());
    }

    private static void initializeFoundFairiesForProfileAndLocation(String profile, String location) {
        foundFairies.computeIfAbsent(profile, profileKey -> new HashMap<>());
        foundFairies.get(profile).computeIfAbsent(location, locationKey -> new HashSet<>());
    }
}
