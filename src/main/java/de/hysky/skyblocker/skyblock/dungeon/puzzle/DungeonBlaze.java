package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * This class provides functionality to render outlines around Blaze entities
 */
public class DungeonBlaze extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonBlaze.class.getName());
	private static final float[] GREEN_COLOR_COMPONENTS = {0.0F, 1.0F, 0.0F};
	private static final float[] WHITE_COLOR_COMPONENTS = {1.0f, 1.0f, 1.0f};
	@SuppressWarnings("unused")
	private static final DungeonBlaze INSTANCE = new DungeonBlaze();

	private static @Nullable ArmorStand highestBlaze = null;
	private static @Nullable ArmorStand lowestBlaze = null;
	private static @Nullable ArmorStand nextHighestBlaze = null;
	private static @Nullable ArmorStand nextLowestBlaze = null;

	private DungeonBlaze() {
		super("blaze", "blaze-room-1-high", "blaze-room-1-low");
	}

	@Init
	public static void init() {
	}

	/**
	 * Updates the state of Blaze entities and triggers the rendering process if necessary.
	 */
	@Override
	public void tick(Minecraft client) {
		if (!shouldSolve()) {
			return;
		}
		if (client.level == null || client.player == null || !Utils.isInDungeons()) return;
		List<ObjectIntPair<ArmorStand>> blazes = getBlazesInWorld(client.level, client.player);
		sortBlazes(blazes);
		updateBlazeEntities(blazes);
	}

	/**
	 * Retrieves Blaze entities in the world and parses their health information.
	 *
	 * @param world The client world to search for Blaze entities.
	 * @return A list of Blaze entities and their associated health.
	 */
	private static List<ObjectIntPair<ArmorStand>> getBlazesInWorld(ClientLevel world, LocalPlayer player) {
		List<ObjectIntPair<ArmorStand>> blazes = new ArrayList<>();
		for (ArmorStand blaze : world.getEntitiesOfClass(ArmorStand.class, player.getBoundingBox().inflate(500D), EntitySelector.ENTITY_NOT_BEING_RIDDEN)) {
			String blazeName = blaze.getName().getString();
			if (blazeName.contains("Blaze") && blazeName.contains("/")) {
				try {
					int health = Integer.parseInt((blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1)).replaceAll(",", ""));
					blazes.add(ObjectIntPair.of(blaze, health));
				} catch (NumberFormatException e) {
					handleException(e);
				}
			}
		}
		return blazes;
	}

	/**
	 * Sorts the Blaze entities based on their health values.
	 *
	 * @param blazes The list of Blaze entities to be sorted.
	 */
	private static void sortBlazes(List<ObjectIntPair<ArmorStand>> blazes) {
		blazes.sort(Comparator.comparingInt(ObjectIntPair::rightInt));
	}

	/**
	 * Updates information about Blaze entities based on sorted list.
	 *
	 * @param blazes The sorted list of Blaze entities with associated health values.
	 */
	private static void updateBlazeEntities(List<ObjectIntPair<ArmorStand>> blazes) {
		if (!blazes.isEmpty()) {
			lowestBlaze = blazes.getFirst().left();
			int highestIndex = blazes.size() - 1;
			highestBlaze = blazes.get(highestIndex).left();
			if (blazes.size() > 1) {
				nextLowestBlaze = blazes.get(1).left();
				nextHighestBlaze = blazes.get(highestIndex - 1).left();
			}
		}
	}

	/**
	 * Extracts outlines for Blaze entities based on health and position.
	 */
	@Override
	public void extractRendering(PrimitiveCollector collector) {
		try {
			if (highestBlaze != null && lowestBlaze != null && highestBlaze.isAlive() && lowestBlaze.isAlive() && SkyblockerConfigManager.get().dungeons.puzzleSolvers.blazeSolver) {
				if (highestBlaze.getY() < 69) {
					extractBlazeOutline(highestBlaze, nextHighestBlaze, collector);
				}
				if (lowestBlaze.getY() > 69) {
					extractBlazeOutline(lowestBlaze, nextLowestBlaze, collector);
				}
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	/**
	 * Extracts outlines for Blaze entities and connections between them.
	 *
	 * @param blaze     The Blaze entity for which to render an outline.
	 * @param nextBlaze The next Blaze entity for connection rendering.
	 */
	private static void extractBlazeOutline(ArmorStand blaze, @Nullable ArmorStand nextBlaze, PrimitiveCollector collector) {
		AABB blazeBox = blaze.getBoundingBox().inflate(0.3, 0.9, 0.3).move(0, -1.1, 0);
		collector.submitOutlinedBox(blazeBox, GREEN_COLOR_COMPONENTS, 5f, false);

		if (nextBlaze != null && nextBlaze.isAlive() && nextBlaze != blaze) {
			AABB nextBlazeBox = nextBlaze.getBoundingBox().inflate(0.3, 0.9, 0.3).move(0, -1.1, 0);
			collector.submitOutlinedBox(nextBlazeBox, WHITE_COLOR_COMPONENTS, 5f, false);

			Vec3 blazeCenter = blazeBox.getCenter();
			Vec3 nextBlazeCenter = nextBlazeBox.getCenter();

			collector.submitLinesFromPoints(new Vec3[]{blazeCenter, nextBlazeCenter}, WHITE_COLOR_COMPONENTS, 1f, 5f, false);
		}
	}

	/**
	 * Handles exceptions by logging and printing stack traces.
	 *
	 * @param e The exception to handle.
	 */
	private static void handleException(Exception e) {
		LOGGER.error("[Skyblocker BlazeRenderer] Encountered an unknown exception", e);
	}
}
