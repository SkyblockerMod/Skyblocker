package de.hysky.skyblocker.skyblock.rift;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EnigmaSouls {
	private static final Logger LOGGER = LoggerFactory.getLogger(EnigmaSouls.class);
	private static final Identifier WAYPOINTS_JSON = new Identifier(SkyblockerMod.NAMESPACE, "rift/enigma_soul_waypoints.json");
	private static final BlockPos[] SOUL_WAYPOINTS = new BlockPos[42];
	private static final Path FOUND_SOULS_FILE = SkyblockerMod.CONFIG_DIR.resolve("found_enigma_souls.json");
	private static final Object2ObjectOpenHashMap<String, ObjectOpenHashSet<BlockPos>> FOUND_SOULS = new Object2ObjectOpenHashMap<>();
	private static final float[] GREEN = DyeColor.GREEN.getColorComponents();
	private static final float[] RED = DyeColor.RED.getColorComponents();
	
	private static CompletableFuture<Void> soulsLoaded;
	
	static void load(MinecraftClient client) {
		//Load waypoints
		soulsLoaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(WAYPOINTS_JSON)) {
				JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
				JsonArray waypoints = file.get("waypoints").getAsJsonArray();
				
				for (int i = 0; i < waypoints.size(); i++) {
					JsonObject waypoint = waypoints.get(i).getAsJsonObject();
					SOUL_WAYPOINTS[i] = new BlockPos(waypoint.get("x").getAsInt(), waypoint.get("y").getAsInt(), waypoint.get("z").getAsInt());
				}
				
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] There was an error while loading enigma soul waypoints! Exception: {}", e);
			}
			
			//Load found souls
			try (BufferedReader reader = Files.newBufferedReader(FOUND_SOULS_FILE)) {
				for (Map.Entry<String, JsonElement> profile : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
					ObjectOpenHashSet<BlockPos> foundSoulsOnProfile = new ObjectOpenHashSet<>();
					
					for (JsonElement foundSoul : profile.getValue().getAsJsonArray().asList()) {
						foundSoulsOnProfile.add(PosUtils.parsePosString(foundSoul.getAsString()));
					}
					
					FOUND_SOULS.put(profile.getKey(), foundSoulsOnProfile);
				}
			} catch (NoSuchFileException ignored) {
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] There was an error while loading found enigma souls! Exception: {}", e);
			}
		});
	}
	
	static void save(MinecraftClient client) {
		JsonObject json = new JsonObject();
		
		for (Map.Entry<String, ObjectOpenHashSet<BlockPos>> foundSoulsForProfile : FOUND_SOULS.entrySet()) {
			JsonArray foundSoulsJson = new JsonArray();
			
			for (BlockPos foundSoul : foundSoulsForProfile.getValue()) {
				foundSoulsJson.add(PosUtils.getPosString(foundSoul));
			}
			
			json.add(foundSoulsForProfile.getKey(), foundSoulsJson);
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(FOUND_SOULS_FILE)) {
			SkyblockerMod.GSON.toJson(json, writer);
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] There was an error while saving found enigma souls! Exception: {}", e);
		}
	}
	
	static void render(WorldRenderContext wrc) {
		SkyblockerConfig.Rift config = SkyblockerConfigManager.get().locations.rift;
		
		if (Utils.isInTheRift() && config.enigmaSoulWaypoints && soulsLoaded.isDone()) {
			for (BlockPos pos : SOUL_WAYPOINTS) {
				if (isSoulMissing(pos)) {
					RenderHelper.renderFilledThroughWallsWithBeaconBeam(wrc, pos, GREEN, 0.5f);
				} else if (config.highlightFoundEnigmaSouls) {
					RenderHelper.renderFilledThroughWallsWithBeaconBeam(wrc, pos, RED, 0.5f);
				}
			}
		}
	}
	
	static void onMessage(Text text, boolean overlay) {
		if (Utils.isInTheRift() && !overlay) {
			String message = text.getString();
			
			if (message.equals("You have already found that Enigma Soul!") || Formatting.strip(message).equals("SOUL! You unlocked an Enigma Soul!")) markClosestSoulAsFound();
		}
	}
	
	static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("rift")
						.then(literal("enigmaSouls")
								.then(literal("markAllFound").executes(context -> {
									markAllFound();
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.rift.enigmaSouls.markAllFound")));
									
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("markAllMissing").executes(context -> {
									markAllMissing();
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.rift.enigmaSouls.markAllMissing")));
									
									return Command.SINGLE_SUCCESS;
								})))));
	}
	
	@SuppressWarnings("resource")
	private static void markClosestSoulAsFound() {
		if (!soulsLoaded.isDone()) return;
		
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		
		Arrays.stream(SOUL_WAYPOINTS)
		.filter(EnigmaSouls::isSoulMissing)
		.min(Comparator.comparingDouble(soulPos -> soulPos.getSquaredDistance(player.getPos())))
		.filter(soulPos -> soulPos.getSquaredDistance(player.getPos()) <= 16)
		.ifPresent(soulPos -> {
			FOUND_SOULS.computeIfAbsent(Utils.getProfile(), profile -> new ObjectOpenHashSet<>());
			FOUND_SOULS.get(Utils.getProfile()).add(soulPos);
		});
	}
	
	private static boolean isSoulMissing(BlockPos soulPos) {
		ObjectOpenHashSet<BlockPos> foundSoulsOnProfile = FOUND_SOULS.get(Utils.getProfile());
		
		return foundSoulsOnProfile == null || !foundSoulsOnProfile.contains(soulPos);
	}
	
	private static void markAllFound() {
		FOUND_SOULS.computeIfAbsent(Utils.getProfile(), profile -> new ObjectOpenHashSet<>());
		FOUND_SOULS.get(Utils.getProfile()).addAll(List.of(SOUL_WAYPOINTS));
	}
	
	private static void markAllMissing() {
		ObjectOpenHashSet<BlockPos> foundSoulsOnProfile = FOUND_SOULS.get(Utils.getProfile());
		
		if (foundSoulsOnProfile != null) foundSoulsOnProfile.clear();
	}
}
