package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.gson.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Waypoints {
    public static final Logger LOGGER = LoggerFactory.getLogger(Waypoints.class);
    private static final Codec<List<WaypointGroup>> CODEC = WaypointGroup.CODEC.listOf();
    private static final Codec<List<WaypointGroup>> SKYTILS_CODEC = WaypointGroup.SKYTILS_CODEC.listOf();
	private static final Codec<Collection<WaypointGroup>> SKYBLOCKER_LEGACY_ORDERED_CODEC = Codec.unboundedMap(Codec.STRING, WaypointGroup.SKYBLOCKER_LEGACY_ORDERED_CODEC).xmap(Map::values, groups -> groups.stream().collect(Collectors.toMap(WaypointGroup::name, Function.identity())));
    private static final String PREFIX = "[Skyblocker-Waypoint-Data-V1]";
	private static final String SKYBLOCKER_LEGACY_ORDERED = "[Skyblocker::OrderedWaypoints::v1]";
    protected static final SystemToast.Type WAYPOINTS_TOAST_TYPE = new SystemToast.Type();

    private static final Path WAYPOINTS_FILE = SkyblockerMod.CONFIG_DIR.resolve("waypoints.json");
	private static final Path SKYBLOCKER_LEGACY_ORDERED_FILE = SkyblockerMod.CONFIG_DIR.resolve("ordered_waypoints.json");
    private static final Multimap<Location, WaypointGroup> waypoints = MultimapBuilder.enumKeys(Location.class).arrayListValues().build();

    @Init
    public static void init() {
        loadWaypoints();
        ClientLifecycleEvents.CLIENT_STOPPING.register(Waypoints::saveWaypoints);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Waypoints::render);
        ClientCommandRegistrationCallback.EVENT.register(Waypoints::registerCommands);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
    }

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("waypoints").executes(Scheduler.queueOpenScreenCommand(() -> new WaypointsScreen(MinecraftClient.getInstance().currentScreen)))
						.then(literal("ordered").then(argument("action", OrderedAction.ArgumentType.orderedAction()).executes(Waypoints::executeOrderedWaypointAction)))
				));
	}

	private static int executeOrderedWaypointAction(CommandContext<FabricClientCommandSource> context) {
		Optional<WaypointGroup> groupOptional = waypoints.get(Utils.getLocation()).stream()
				.filter(group -> group.ordered() && !group.waypoints().isEmpty() && group.waypoints().stream().allMatch(Waypoint::isEnabled))
				.findFirst();
		if (groupOptional.isEmpty()) {
			context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.literal("No ordered group enabled here! (make sure all waypoints in the group are enabled)")));
			return Command.SINGLE_SUCCESS;
		}
		WaypointGroup group = groupOptional.get();
		OrderedAction action = OrderedAction.ArgumentType.getOrderedAction(context, "action");
		int index = group.currentIndex();
		int waypointCount = group.waypoints().size();
		switch (action) {
			case FIRST, RESET -> group.resetCurrentIndex();
			case NEXT -> group.setCurrentIndex((index + 1) % waypointCount);
			case PREVIOUS -> group.setCurrentIndex((index - 1 + waypointCount) % waypointCount);
		}
		return Command.SINGLE_SUCCESS;
	}

    public static void loadWaypoints() {
        waypoints.clear();
        try (BufferedReader reader = Files.newBufferedReader(WAYPOINTS_FILE)) {
            List<WaypointGroup> waypointGroups = CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(reader, JsonArray.class)).resultOrPartial(LOGGER::error).orElseThrow();
            waypointGroups.forEach(Waypoints::putWaypointGroup);
		} catch (NoSuchFileException ignored) {
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while loading waypoints", e);
        }
		try (BufferedReader reader = Files.newBufferedReader(SKYBLOCKER_LEGACY_ORDERED_FILE)) {
			Collection<WaypointGroup> waypointGroups = SKYBLOCKER_LEGACY_ORDERED_CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(reader, JsonObject.class)).resultOrPartial(LOGGER::error).orElseThrow();
			for (WaypointGroup group : waypointGroups) {
				Waypoints.putWaypointGroup(group.withIsland(Location.DWARVEN_MINES).deepCopy());
				Waypoints.putWaypointGroup(group.withIsland(Location.CRYSTAL_HOLLOWS).deepCopy());
			}
			Files.move(SKYBLOCKER_LEGACY_ORDERED_FILE, SkyblockerMod.CONFIG_DIR.resolve("legacy_ordered_waypoints.json"));
			LOGGER.info("[Skyblocker Waypoints] Successfully migrated {} ordered waypoints from {} groups to waypoints!", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size());
		} catch (NoSuchFileException | FileAlreadyExistsException ignored) {
		} catch (IOException e) {
			LOGGER.error("[Skyblocker Waypoints] Encountered exception while loading legacy ordered waypoints", e);
		}
	}

    public static void saveWaypoints(MinecraftClient client) {
        try (BufferedWriter writer = Files.newBufferedWriter(WAYPOINTS_FILE)) {
            JsonElement waypointsJson = CODEC.encodeStart(JsonOps.INSTANCE, List.copyOf(waypoints.values())).resultOrPartial(LOGGER::error).orElseThrow();
            SkyblockerMod.GSON.toJson(waypointsJson, writer);
            LOGGER.info("[Skyblocker Waypoints] Saved waypoints");
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while saving waypoints", e);
        }
    }

    public static @Nullable List<WaypointGroup> fromSkyblocker(String waypointsString, Location defaultIsland) {
        waypointsString = waypointsString.trim();
        if (waypointsString.startsWith(PREFIX)) {
            try (GZIPInputStream reader = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(waypointsString.replace(PREFIX, ""))))) {
                return CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(new String(reader.readAllBytes()), JsonArray.class)).resultOrPartial(LOGGER::error).orElseThrow();
            } catch (IOException e) {
                LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skyblocker waypoint data", e);
				return null;
            }
        } else if (waypointsString.startsWith(SKYBLOCKER_LEGACY_ORDERED)) {
			try (GZIPInputStream reader = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(waypointsString.replace(SKYBLOCKER_LEGACY_ORDERED, ""))))) {
				return applyDefaultLocation(SKYBLOCKER_LEGACY_ORDERED_CODEC.parse(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(new String(reader.readAllBytes()), JsonObject.class)).resultOrPartial(LOGGER::error).orElseThrow(), defaultIsland);
			} catch (IOException e) {
				LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skyblocker legacy ordered waypoint data", e);
				return null;
			}
		}
		LOGGER.error("[Skyblocker Waypoints] Unknown skyblocker waypoint data prefix");
        return null;
    }

    public static String toSkyblocker(List<WaypointGroup> waypointGroups) {
        String waypointsJson = SkyblockerMod.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE, waypointGroups).resultOrPartial(LOGGER::error).orElseThrow());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)){
            gzip.write(waypointsJson.getBytes());
        } catch (IOException e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while serializing Skyblocker waypoint data", e);
        }
        return PREFIX + new String(Base64.getEncoder().encode(output.toByteArray()));
    }

    public static @Nullable List<WaypointGroup> fromSkytils(String waypointsString, Location defaultIsland) {
        waypointsString = waypointsString.trim();
        try {
            if (waypointsString.startsWith("<Skytils-Waypoint-Data>(V")) {
                int version = Integer.parseInt(waypointsString.substring(25, waypointsString.indexOf(')')));
                waypointsString = waypointsString.substring(waypointsString.indexOf(':') + 1);
                if (version == 1) {
                    try (GZIPInputStream reader = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(waypointsString)))) {
                        return fromSkytilsJson(IOUtils.toString(reader, StandardCharsets.UTF_8), defaultIsland);
                    }
                } else {
                    LOGGER.error("[Skyblocker Waypoints] Unknown Skytils waypoint data version: {}", version);
                }
            } else return fromSkytilsJson(new String(Base64.getDecoder().decode(waypointsString)), defaultIsland);
        } catch (NumberFormatException e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skytils waypoint data version", e);
			return null;
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Waypoints] Encountered exception while decoding Skytils waypoint data", e);
			return null;
        }
        return Collections.emptyList();
    }

    public static List<WaypointGroup> fromSkytilsJson(String waypointGroupsString, Location defaultIsland) {
        JsonArray waypointGroupsJson;
        try {
            waypointGroupsJson = SkyblockerMod.GSON.fromJson(waypointGroupsString, JsonObject.class).getAsJsonArray("categories");
        } catch (JsonSyntaxException e) {
            // Handle the case where there is only a single json list of waypoints and no group data.
            JsonObject waypointGroupJson = new JsonObject();
            waypointGroupJson.addProperty("name", "New Group");
            waypointGroupJson.addProperty("island", defaultIsland.id());
            waypointGroupJson.add("waypoints", SkyblockerMod.GSON.fromJson(waypointGroupsString, JsonArray.class));
            waypointGroupsJson = new JsonArray();
            waypointGroupsJson.add(waypointGroupJson);
        }
        List<WaypointGroup> waypointGroups = SKYTILS_CODEC.parse(JsonOps.INSTANCE, waypointGroupsJson).resultOrPartial(LOGGER::error).orElseThrow();
        return applyDefaultLocation(waypointGroups, defaultIsland);
    }

    public static String toSkytilsBase64(List<WaypointGroup> waypointGroups) {
        return Base64.getEncoder().encodeToString(toSkytilsJson(waypointGroups).getBytes());
    }

    public static String toSkytilsJson(List<WaypointGroup> waypointGroups) {
        JsonObject waypointGroupsJson = new JsonObject();
        waypointGroupsJson.add("categories", SKYTILS_CODEC.encodeStart(JsonOps.INSTANCE, waypointGroups).resultOrPartial(LOGGER::error).orElseThrow());
        return SkyblockerMod.GSON_COMPACT.toJson(waypointGroupsJson);
    }

    public static WaypointGroup fromColeweightJson(String waypointsJson, Location defaultIsland) {
        return WaypointGroup.COLEWEIGHT_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(waypointsJson)).resultOrPartial(LOGGER::error).orElseThrow().withIsland(defaultIsland);
    }

	public static List<WaypointGroup> applyDefaultLocation(Collection<WaypointGroup> waypointGroups, Location defaultIsland) {
		return waypointGroups.stream().map(waypointGroup -> waypointGroup.island() == Location.UNKNOWN ? waypointGroup.withIsland(defaultIsland) : waypointGroup).toList();
	}

	/**
	 * Gets the waypoint groups for the specified island.
	 */
    public static Collection<WaypointGroup> getWaypointGroup(Location island) {
        return waypoints.get(island);
    }

	/**
	 * Puts the waypoint group into the waypoints map based on the group's island.
	 */
    public static boolean putWaypointGroup(WaypointGroup waypointGroup) {
        return waypoints.put(waypointGroup.island(), waypointGroup);
    }

	/**
	 * Clears the waypoints map and puts all the new waypoints into it.
	 */
    public static boolean clearAndPutAllWaypoints(Multimap<Location, WaypointGroup> newWaypoints) {
        waypoints.clear();
        return waypoints.putAll(newWaypoints);
    }

	/**
	 * Gets whether the waypoints map is equal to the given waypoints map.
	 */
    public static boolean areWaypointsEqual(Multimap<Location, WaypointGroup> otherWaypoints) {
        return waypoints.equals(otherWaypoints);
    }

	/**
	 * Deep copies the waypoints map.
	 * Used for copying waypoints to {@link WaypointsScreen} for editing.
	 */
    public static Multimap<Location, WaypointGroup> waypointsDeepCopy() {
        return waypoints.values().stream().map(WaypointGroup::deepCopy).collect(Multimaps.toMultimap(WaypointGroup::island, Function.identity(), () -> MultimapBuilder.enumKeys(Location.class).arrayListValues().build()));
    }

    private static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().uiAndVisuals.waypoints.enableWaypoints) {
            for (WaypointGroup group : getWaypointGroup(Utils.getLocation())) {
                if (group != null) {
                    group.render(context);
                }
            }
            if (Utils.getLocationRaw().isEmpty()) return;
            for (WaypointGroup group : getWaypointGroup(Location.UNKNOWN)) {
                if (group != null) {
                    group.render(context);
                }
            }
        }
    }

	private static void reset() {
		waypoints.values().forEach(WaypointGroup::resetCurrentIndex);
	}

	private enum OrderedAction implements StringIdentifiable {
		NEXT,
		PREVIOUS,
		FIRST,
		RESET;

		private static final Codec<OrderedAction> CODEC = StringIdentifiable.createCodec(OrderedAction::values);

		@Override
		public String asString() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		static class ArgumentType extends EnumArgumentType<OrderedAction> {
			protected ArgumentType() {
				super(CODEC, OrderedAction::values);
			}

			static ArgumentType orderedAction() {
				return new ArgumentType();
			}

			static <S> OrderedAction getOrderedAction(CommandContext<S> context, String name) {
				return context.getArgument(name, OrderedAction.class);
			}
		}
	}
}
