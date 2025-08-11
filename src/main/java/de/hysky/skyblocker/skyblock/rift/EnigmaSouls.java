package de.hysky.skyblocker.skyblock.rift;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.OtherLocationsConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EnigmaSouls {
	private static final Logger LOGGER = LoggerFactory.getLogger(EnigmaSouls.class);
	private static final Supplier<Waypoint.Type> TYPE_SUPPLIER = () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType;
	private static final Identifier WAYPOINTS_JSON = Identifier.of(SkyblockerMod.NAMESPACE, "rift/enigma_soul_waypoints.json");
	private static final Map<BlockPos, ProfileAwareWaypoint> SOUL_WAYPOINTS = new HashMap<>(42);
	private static final Path FOUND_SOULS_FILE = SkyblockerMod.CONFIG_DIR.resolve("found_enigma_souls.json");
	private static final float[] GREEN = ColorUtils.getFloatComponents(DyeColor.GREEN);
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);

	private static CompletableFuture<Void> soulsLoaded;

	static void load(MinecraftClient client) {
		//Load waypoints
		soulsLoaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(WAYPOINTS_JSON)) {
				JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
				JsonArray waypoints = file.get("waypoints").getAsJsonArray();

				for (int i = 0; i < waypoints.size(); i++) {
					JsonObject waypoint = waypoints.get(i).getAsJsonObject();
					BlockPos pos = new BlockPos(waypoint.get("x").getAsInt(), waypoint.get("y").getAsInt(), waypoint.get("z").getAsInt());
					SOUL_WAYPOINTS.put(pos, new EnigmaSoul(pos, TYPE_SUPPLIER, GREEN, RED));
				}

			} catch (IOException e) {
				LOGGER.error("[Skyblocker] There was an error while loading enigma soul waypoints!", e);
			}

			//Load found souls
			try (BufferedReader reader = Files.newBufferedReader(FOUND_SOULS_FILE)) {
				for (Map.Entry<String, JsonElement> profile : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
					for (JsonElement foundSoul : profile.getValue().getAsJsonArray().asList()) {
						SOUL_WAYPOINTS.get(PosUtils.parsePosString(foundSoul.getAsString())).setFound(profile.getKey());
					}
				}
			} catch (NoSuchFileException ignored) {
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] There was an error while loading found enigma souls!", e);
			}
		});
	}

	static void save(MinecraftClient client) {
		Map<String, Set<BlockPos>> foundSouls = new HashMap<>();
		for (ProfileAwareWaypoint soul : SOUL_WAYPOINTS.values()) {
			for (String profile : soul.foundProfiles) {
				foundSouls.computeIfAbsent(profile, profile_ -> new HashSet<>());
				foundSouls.get(profile).add(soul.pos);
			}
		}

		JsonObject json = new JsonObject();
		for (Map.Entry<String, Set<BlockPos>> foundSoulsForProfile : foundSouls.entrySet()) {
			JsonArray foundSoulsJson = new JsonArray();

			for (BlockPos foundSoul : foundSoulsForProfile.getValue()) {
				foundSoulsJson.add(PosUtils.getPosString(foundSoul));
			}

			json.add(foundSoulsForProfile.getKey(), foundSoulsJson);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(FOUND_SOULS_FILE)) {
			SkyblockerMod.GSON.toJson(json, writer);
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] There was an error while saving found enigma souls!", e);
		}
	}

	static void render(WorldRenderContext context) {
		OtherLocationsConfig.Rift config = SkyblockerConfigManager.get().otherLocations.rift;

		if (Utils.isInTheRift() && config.enigmaSoulWaypoints && soulsLoaded.isDone()) {
			for (Waypoint soul : SOUL_WAYPOINTS.values()) {
				if (soul.shouldRender() || config.highlightFoundEnigmaSouls) {
					soul.render(context);
				}
			}
		}
	}

	static void onMessage(Text text, boolean overlay) {
		if (Utils.isInTheRift() && !overlay) {
			String message = text.getString();

			if (message.equals("You have already found that Enigma Soul!") || Formatting.strip(message).equals("SOUL! You unlocked an Enigma Soul!"))
				markClosestSoulAsFound();
		}
	}

	static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("rift")
						.then(literal("enigmaSouls")
								.then(literal("markAllFound").executes(context -> {
									SOUL_WAYPOINTS.values().forEach(Waypoint::setFound);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.rift.enigmaSouls.markAllFound")));

									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("markAllMissing").executes(context -> {
									SOUL_WAYPOINTS.values().forEach(Waypoint::setMissing);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.rift.enigmaSouls.markAllMissing")));

									return Command.SINGLE_SUCCESS;
								})))));
	}

	private static void markClosestSoulAsFound() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (!soulsLoaded.isDone() || player == null) return;

		SOUL_WAYPOINTS.values().stream()
				.filter(Waypoint::shouldRender)
				.min(Comparator.comparingDouble(soul -> soul.pos.getSquaredDistance(player.getPos())))
				.filter(soul -> soul.pos.getSquaredDistance(player.getPos()) <= 16)
				.ifPresent(Waypoint::setFound);
	}

	private static class EnigmaSoul extends ProfileAwareWaypoint {
		private EnigmaSoul(BlockPos pos, Supplier<Type> typeSupplier, float[] missingColor, float[] foundColor) {
			super(pos, typeSupplier, missingColor, foundColor);
		}

		@Override
		public boolean shouldRender() {
			return super.shouldRender() || SkyblockerConfigManager.get().otherLocations.rift.highlightFoundEnigmaSouls;
		}
	}
}
