package de.hysky.skyblocker.skyblock.rift;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.OtherLocationsConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.waypoint.ProfileAwareWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.hysky.skyblocker.utils.command.CommandUtils.failOnMissingProfile;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class EnigmaSouls {
	private static final Logger LOGGER = LoggerFactory.getLogger(EnigmaSouls.class);
	private static final Supplier<Waypoint.Type> TYPE_SUPPLIER = () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType;
	private static final Identifier WAYPOINTS_JSON = SkyblockerMod.id("rift/enigma_soul_waypoints.json");
	private static final Map<RiftZone, Map<BlockPos, ProfileAwareWaypoint>> SOUL_WAYPOINTS = HashMap.newHashMap(9);
	private static final Path FOUND_SOULS_FILE = SkyblockerMod.CONFIG_DIR.resolve("found_enigma_souls.json");
	private static final float[] GREEN = ColorUtils.getFloatComponents(DyeColor.GREEN);
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);

	private static CompletableFuture<Void> soulsLoaded;

	static void load(Minecraft client) {
		//Load waypoints
		soulsLoaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(WAYPOINTS_JSON)) {
				JsonObject zones = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("zones");

				for (Map.Entry<String, JsonElement> zoneJson : zones.entrySet()) {
					RiftZone zone = RiftZone.fromSerializedName(zoneJson.getKey());
					JsonArray waypoints_list = zoneJson.getValue().getAsJsonArray();
					Map<BlockPos, ProfileAwareWaypoint> waypoints = HashMap.newHashMap(waypoints_list.size());

					for (JsonElement wp : waypoints_list) {
						JsonObject waypoint = wp.getAsJsonObject();
						BlockPos pos = new BlockPos(waypoint.get("x").getAsInt(), waypoint.get("y").getAsInt(), waypoint.get("z").getAsInt());
						waypoints.put(pos, new EnigmaSoul(pos, zone, waypoint.get("name").getAsString()));
					}

					SOUL_WAYPOINTS.put(zone, waypoints);
				}
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] There was an error while loading enigma soul waypoints!", e);
			}

			LOGGER.info("[Skyblocker] Loaded {} enigma souls across {} locations", SOUL_WAYPOINTS.values().stream().mapToInt(Map::size).sum(), SOUL_WAYPOINTS.size());

			if (SOUL_WAYPOINTS.size() != RiftZone.values().length) {
				LOGGER.debug("[Skyblocker] Zones from enigma soul json do not match RiftZone enum!");
			}

			//Load found souls
			try (BufferedReader reader = Files.newBufferedReader(FOUND_SOULS_FILE)) {
				for (Map.Entry<String, JsonElement> profile : JsonParser.parseReader(reader).getAsJsonObject().asMap().entrySet()) {
					for (JsonElement foundSoul : profile.getValue().getAsJsonArray()) {
						BlockPos pos = PosUtils.parsePosString(foundSoul.getAsString());
						for (Map<BlockPos, ProfileAwareWaypoint> zone : SOUL_WAYPOINTS.values()) {
							ProfileAwareWaypoint waypoint = zone.get(pos);
							if (waypoint != null) {
								waypoint.setFound(profile.getKey());
								break;
							}
						}
					}
				}

				LOGGER.debug("[Skyblocker] Loaded found enigma souls");
			} catch (NoSuchFileException _) {
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] There was an error while loading found enigma souls!", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	static void save(Minecraft client) {
		Map<String, Set<BlockPos>> foundSouls = new HashMap<>();
		streamWaypoints().forEach(soul -> {
			for (String profile : soul.foundProfiles) {
				foundSouls.computeIfAbsent(profile, _ -> new HashSet<>());
				foundSouls.get(profile).add(soul.pos);
			}
		});

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
			LOGGER.info("[Skyblocker] Saved found enigma souls");
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] There was an error while saving found enigma souls!", e);
		}
	}

	static void extractRendering(PrimitiveCollector collector) {
		OtherLocationsConfig.Rift config = SkyblockerConfigManager.get().otherLocations.rift;

		if (Utils.isInTheRift() && config.enigmaSoulWaypoints && soulsLoaded.isDone()) {
			streamWaypoints().forEach(soul -> {
				if (soul.shouldRender()) {
					soul.extractRendering(collector);
				}
			});
		}
	}

	static boolean onMessage(Component text, boolean overlay) {
		if (Utils.isInTheRift() && !overlay) {
			String message = text.getString();

			if (message.equals("You have already found that Enigma Soul!") || ChatFormatting.stripFormatting(message).equals("SOUL! You unlocked an Enigma Soul!"))
				markClosestSoul(true);
		}

		return true;
	}

	static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("rift")
						.then(literal("enigmaSouls")
								.then(literal("markAllFound").executes(context -> {
									if (failOnMissingProfile(context)) return 0;

									streamWaypoints().forEach(Waypoint::setFound);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.rift.enigmaSouls.markAllFound")));

									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("markAllMissing").executes(context -> {
									if (failOnMissingProfile(context)) return 0;

									streamWaypoints().forEach(Waypoint::setMissing);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.rift.enigmaSouls.markAllMissing")));

									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("markClosestFound").executes(context -> {
									if (failIfNotInRift(context)) return 0;

									markClosestSoul(true);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.rift.enigmaSouls.markClosestFound")));

									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("markClosestMissing").executes(context -> {
									if (failIfNotInRift(context)) return 0;

									markClosestSoul(false);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.rift.enigmaSouls.markClosestMissing")));

									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("markZoneFound").then(argument("zone", RiftZone.RiftZoneArgumentType.riftZone()).executes(context -> {
									if (failOnMissingProfile(context)) return 0;

									RiftZone zone = context.getArgument("zone", RiftZone.class);
									SOUL_WAYPOINTS.get(zone).values().forEach(Waypoint::setFound);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(
											Component.translatableEscape("skyblocker.rift.enigmaSouls.markZoneFound", zone.displayName())));

									return Command.SINGLE_SUCCESS;
								})))
								.then(literal("markZoneMissing").then(argument("zone", RiftZone.RiftZoneArgumentType.riftZone()).executes(context -> {
									if (failOnMissingProfile(context)) return 0;

									RiftZone zone = context.getArgument("zone", RiftZone.class);
									SOUL_WAYPOINTS.get(zone).values().forEach(Waypoint::setMissing);
									context.getSource().sendFeedback(Constants.PREFIX.get().append(
										Component.translatableEscape("skyblocker.rift.enigmaSouls.markZoneMissing", zone.displayName())));

									return Command.SINGLE_SUCCESS;
								})))
								)));
	}

	private static void markClosestSoul(boolean asFound) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (!soulsLoaded.isDone() || player == null || !Utils.isInTheRift()) return;

		streamWaypoints()
				.min(Comparator.comparingDouble(soul -> soul.pos.distToCenterSqr(player.position())))
				.filter(soul -> soul.pos.distToCenterSqr(player.position()) <= 16)
				.ifPresent(asFound ? Waypoint::setFound : Waypoint::setMissing);
	}

	public static boolean failIfNotInRift(CommandContext<FabricClientCommandSource> context) {
		if (!Utils.isInTheRift()) {
			context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.rift.notInRift").withStyle(ChatFormatting.RED)));
			return true;
		}
		return false;
	}

	private static Stream<ProfileAwareWaypoint> streamWaypoints() {
		return SOUL_WAYPOINTS.values().stream().flatMap(zone -> zone.values().stream());
	}

	private static class EnigmaSoul extends ProfileAwareWaypoint {
		public final RiftZone zone;
		public final String name;

		private EnigmaSoul(BlockPos pos, RiftZone zone, String name) {
			super(pos, TYPE_SUPPLIER, GREEN, RED);
			this.zone = zone;
			this.name = name;
		}

		@Override
		public boolean shouldRender() {
			return super.shouldRender() || SkyblockerConfigManager.get().otherLocations.rift.highlightFoundEnigmaSouls;
		}

		@Override
		public void setFound() {
			setFound(Utils.getProfile());
		}

		@Override
		public void setFound(String profile) {
			LOGGER.debug("[Skyblocker] Set enigma soul found for {}: {}/{}", profile, zone.displayName(), name);
			super.setFound(profile);
		}

		@Override
		public void setMissing() {
			LOGGER.debug("[Skyblocker] Set enigma soul missing: {}/{}", zone.displayName(), name);
			super.setMissing();
		}
	}

	private enum RiftZone implements StringRepresentable {
		WYLD_WOODS,
		BLACK_LAGOON,
		WEST_VILLAGE,
		DREADFARM,
		VILLAGE_PLAZA,
		LIVING_CAVE,
		COLOSSEUM,
		STILLGORE_CHATEAU,
		MOUNTAINTOP;

		private static final Codec<RiftZone> CODEC = StringRepresentable.fromEnum(RiftZone::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		public String displayName() {
			return WordUtils.capitalizeFully(name().replace("_", " "));
		}

		public static RiftZone fromSerializedName(String name) {
			return valueOf(name.toUpperCase(Locale.ENGLISH));
		}

		public static class RiftZoneArgumentType extends StringRepresentableArgument<RiftZone> {
			private RiftZoneArgumentType() {
				super(CODEC, RiftZone::values);
			}

			public static RiftZoneArgumentType riftZone() {
				return new RiftZoneArgumentType();
			}
		}
	}
}
