package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.primitives.Floats;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.OrderedNamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * @deprecated Use {@link Waypoints} instead.
 */
@Deprecated
public class OrderedWaypoints {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<Map<String, OrderedWaypointGroup>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING, OrderedWaypointGroup.CODEC).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);
	private static final String PREFIX = "[Skyblocker::OrderedWaypoints::v1]";
	public static final Path PATH = SkyblockerMod.CONFIG_DIR.resolve("ordered_waypoints.json");

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_client -> load());
		ClientCommandRegistrationCallback.EVENT.register(OrderedWaypoints::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("waypoints")
						.then(literal("ordered")
								.then(literal("import")
										.then(literal("skyblocker")
												.executes(context -> fromSkyblockerFormat(context.getSource())))))));
	}

    /**
     * Loads and migrates the ordered waypoints to waypoints.
     * @deprecated Use {@link Waypoints} instead.
     */
    @Deprecated
	private static void load() {
		try (BufferedReader reader = Files.newBufferedReader(PATH)) {
			Map<String, OrderedWaypointGroup> orderedWaypoints = SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
			migrateOrderedWaypoints(orderedWaypoints);
			Files.move(PATH, SkyblockerMod.CONFIG_DIR.resolve("legacy_ordered_waypoints.json"));
			LOGGER.info("[Skyblocker Ordered Waypoints] Successfully migrated {} ordered waypoints from {} groups to waypoints!", orderedWaypoints.values().stream().map(OrderedWaypointGroup::waypoints).mapToInt(List::size).sum(), orderedWaypoints.size());
		} catch (NoSuchFileException ignored) {
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to load the waypoints! :(", e);
		}
	}

    /**
     * @deprecated Use {@link Waypoints} instead.
     */
    @Deprecated
	private static int fromSkyblockerFormat(FabricClientCommandSource source) {
		try {
			String importCode = MinecraftClient.getInstance().keyboard.getClipboard();

			if (importCode.startsWith(PREFIX)) {
				String encoded = importCode.replace(PREFIX, "");
				byte[] decoded = Base64.getDecoder().decode(encoded);

				String json = new String(new GZIPInputStream(new ByteArrayInputStream(decoded)).readAllBytes());
				Map<String, OrderedWaypointGroup> importedWaypoints = SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();

				migrateOrderedWaypoints(importedWaypoints);
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.success")));
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.unknownFormatHeader")));
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to import waypoints!", e);
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.fail")));
		}

		return Command.SINGLE_SUCCESS;
	}

    /**
     * Migrates the given ordered waypoints to waypoints.
     */
	private static void migrateOrderedWaypoints(Map<String, OrderedWaypointGroup> orderedWaypoints) {
		for (OrderedWaypointGroup legacyGroup : orderedWaypoints.values()) {
			// Migrate waypoints to both the dwarven mines and the crystal hollows
			Waypoints.putWaypointGroup(new WaypointGroup(legacyGroup.name, Location.DWARVEN_MINES, legacyGroup.waypoints.stream().map(waypoint -> new OrderedNamedWaypoint(waypoint.pos, "", new float[]{0, 1, 0})).collect(Collectors.toList()), true));
			Waypoints.putWaypointGroup(new WaypointGroup(legacyGroup.name, Location.CRYSTAL_HOLLOWS, legacyGroup.waypoints.stream().map(waypoint -> new OrderedNamedWaypoint(waypoint.pos, "", new float[]{0, 1, 0})).collect(Collectors.toList()), true));
		}
	}

    @Deprecated
	private record OrderedWaypointGroup(String name, boolean enabled, ObjectArrayList<OrderedWaypoint> waypoints) {
		static final Codec<OrderedWaypointGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(OrderedWaypointGroup::name),
				Codec.BOOL.fieldOf("enabled").forGetter(OrderedWaypointGroup::enabled),
				OrderedWaypoint.LIST_CODEC.fieldOf("waypoints").xmap(ObjectArrayList::new, ObjectArrayList::new).forGetter(OrderedWaypointGroup::waypoints)
		).apply(instance, OrderedWaypointGroup::new));
	}

    @Deprecated
	private static class OrderedWaypoint extends Waypoint {
		static final Codec<OrderedWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
						BlockPos.CODEC.fieldOf("pos").forGetter(OrderedWaypoint::getPos),
						Codec.floatRange(0, 1).listOf().xmap(Floats::toArray, FloatArrayList::new).optionalFieldOf("colorComponents", new float[0]).forGetter(inst -> inst.colorComponents.length == 3 ? inst.colorComponents : new float[0]))
				.apply(instance, OrderedWaypoint::new));
		static final Codec<List<OrderedWaypoint>> LIST_CODEC = CODEC.listOf();

		OrderedWaypoint(BlockPos pos, float[] colorComponents) {
			super(pos, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, colorComponents);
		}

		private BlockPos getPos() {
			return this.pos;
		}
	}
}
