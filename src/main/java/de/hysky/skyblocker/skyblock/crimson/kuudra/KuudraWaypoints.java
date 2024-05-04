package de.hysky.skyblocker.skyblock.crimson.kuudra;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint.Type;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class KuudraWaypoints {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final float[] SUPPLIES_COLOR = { 255f / 255f, 0f, 0f };
	private static final float[] PEARL_COLOR = { 57f / 255f, 117f / 255f, 125f / 255f };
	private static final float[] SAFE_SPOT_COLOR = { 255f / 255f, 85f / 255f, 255f / 255f };
	private static final Supplier<Type> SUPPLIES_AND_FUEL_TYPE = () -> SkyblockerConfigManager.get().locations.crimsonIsle.kuudra.suppliesAndFuelWaypointType;
	private static final ObjectArrayList<Waypoint> SAFE_SPOT_WAYPOINTS = new ObjectArrayList<>();
	private static final ObjectArrayList<Waypoint> PEARL_WAYPOINTS = new ObjectArrayList<>();
	private static final Function<float[], Codec<List<Waypoint>>> CODEC = cc -> PosUtils.ALT_BLOCK_POS_CODEC.xmap(
			pos -> new Waypoint(pos, () -> Waypoint.Type.HIGHLIGHT, cc, false),
			waypoint -> waypoint.pos)
			.listOf();

	//Use non final lists and swap them out to avoid ConcurrentModificationExceptions
	private static ObjectArrayList<Waypoint> supplyWaypoints = ObjectArrayList.of();
	private static ObjectArrayList<Waypoint> ballistaBuildWaypoints = ObjectArrayList.of();
	private static ObjectArrayList<Waypoint> fuelWaypoints = ObjectArrayList.of();
	private static boolean loaded;

	static void load(MinecraftClient client) {
		CompletableFuture<Void> safeSpots = loadWaypoints(client, new Identifier(SkyblockerMod.NAMESPACE, "crimson/kuudra/safe_spot_waypoints.json"), SAFE_SPOT_WAYPOINTS, SAFE_SPOT_COLOR);
		CompletableFuture<Void> pearls = loadWaypoints(client, new Identifier(SkyblockerMod.NAMESPACE, "crimson/kuudra/pearl_waypoints.json"), PEARL_WAYPOINTS, PEARL_COLOR);
		
		CompletableFuture.allOf(safeSpots, pearls).whenComplete((_result, _throwable) -> loaded = true);
	}

	private static CompletableFuture<Void> loadWaypoints(MinecraftClient client, Identifier file, ObjectArrayList<Waypoint> list, float[] colorComponents) {
		return CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(file)) {
				return CODEC.apply(colorComponents).parse(JsonOps.INSTANCE, getWaypoints(reader)).getOrThrow();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Kuudra Waypoints] Failed to load kuudra waypoints from: {}", file, e);

				return List.<Waypoint>of();
			}
		}).thenAccept(list::addAll);
	}

	private static JsonElement getWaypoints(BufferedReader reader) {
		return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("waypoints");
	}

	static void tick() {
		MinecraftClient client = MinecraftClient.getInstance();
		SkyblockerConfig.Kuudra config = SkyblockerConfigManager.get().locations.crimsonIsle.kuudra;

		if (Utils.isInKuudra() && (config.supplyWaypoints || config.fuelWaypoints || config.ballistaBuildWaypoints)) {
			Box searchBox = client.player.getBoundingBox().expand(500d);
			ObjectArrayList<Waypoint> supplies = new ObjectArrayList<>();
			ObjectArrayList<Waypoint> fuelCells = new ObjectArrayList<>();

			if (config.supplyWaypoints || config.fuelWaypoints) {
				List<GiantEntity> giants = client.world.getEntitiesByClass(GiantEntity.class, searchBox, giant -> giant.getY() < 67);

				for (GiantEntity giant : giants) {
					double yawOffset = giant.getYaw() + 115;

					double x = giant.getX() + 4.5 * Math.cos((yawOffset) * MathHelper.RADIANS_PER_DEGREE);
					double y = 75;
					double z = giant.getZ() + 4.5 * Math.sin((yawOffset) * MathHelper.RADIANS_PER_DEGREE);

					Waypoint waypoint = new Waypoint(BlockPos.ofFloored(x, y, z), SUPPLIES_AND_FUEL_TYPE, SUPPLIES_COLOR, false);

					switch (Kuudra.phase) {
						case RETRIEVE_SUPPLIES -> supplies.add(waypoint);
						case DPS -> fuelCells.add(waypoint);
						default -> supplies.add(waypoint);
					}
				}
			}

			ObjectArrayList<Waypoint> ballistaBuildSpots = new ObjectArrayList<>();

			if (config.ballistaBuildWaypoints) {
				List<ArmorStandEntity> armorStands = client.world.getEntitiesByClass(ArmorStandEntity.class, searchBox, ArmorStandEntity::hasCustomName);

				for (ArmorStandEntity armorStand : armorStands) {
					String name = armorStand.getName().getString();

					if (config.ballistaBuildWaypoints && name.contains("SNEAK + PUNCH")) {
						ballistaBuildSpots.add(new Waypoint(armorStand.getBlockPos(), () -> Waypoint.Type.WAYPOINT, SUPPLIES_COLOR, false));
					}
				}
			}

			supplyWaypoints = supplies;
			ballistaBuildWaypoints = ballistaBuildSpots;
			fuelWaypoints = fuelCells;
		}
	}

	static void render(WorldRenderContext context) {
		SkyblockerConfig.Kuudra config = SkyblockerConfigManager.get().locations.crimsonIsle.kuudra;

		if (Utils.isInKuudra() && loaded) {
			if (config.supplyWaypoints) {
				for (Waypoint waypoint : supplyWaypoints) {
					waypoint.render(context);
				}
			}

			if (config.ballistaBuildWaypoints) {
				for (Waypoint waypoint : ballistaBuildWaypoints) {
					waypoint.render(context);
				}
			}
			
			if (config.fuelWaypoints) {
				for (Waypoint waypoint : fuelWaypoints) {
					waypoint.render(context);
				}
			}

			if (config.safeSpotWaypoints) {
				for (Waypoint waypoint : SAFE_SPOT_WAYPOINTS) {
					waypoint.render(context);
				}
			}

			//TODO maybe have "dynamic" waypoints that draw a line to the actual spot
			if (config.pearlWaypoints) {
				for (Waypoint waypoint : PEARL_WAYPOINTS) {
					waypoint.render(context);
				}
			}
		}
	}
}
