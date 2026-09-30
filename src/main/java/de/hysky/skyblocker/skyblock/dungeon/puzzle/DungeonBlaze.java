package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

/**
 * This class provides functionality to render outlines around Blaze entities
 */
public class DungeonBlaze extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonBlaze.class.getName());
	private static final float[] GREEN_COLOR_COMPONENTS = {0.0f, 1.0f, 0.0f};
	private static final float[] WHITE_COLOR_COMPONENTS = {1.0f, 1.0f, 1.0f};
	@SuppressWarnings("unused")
	private static final DungeonBlaze INSTANCE = new DungeonBlaze();

	private static @Nullable ArmorStand highestBlaze = null;
	private static @Nullable ArmorStand lowestBlaze = null;
	private static @Nullable ArmorStand nextHighestBlaze = null;
	private static @Nullable ArmorStand nextLowestBlaze = null;
	private static List<ArmorStand> sortedBlazes = List.of();

	private DungeonBlaze() {
		super("blaze", "blaze-room-1-high", "blaze-room-1-low");
	}

	@Init
	public static void init() {
		UseItemCallback.EVENT.register(DungeonBlaze::onUseItem);
		AttackEntityCallback.EVENT.register(DungeonBlaze::onAttackEntity);
	}

	private static boolean shouldBlockWrongShots() {
		return INSTANCE.shouldSolve()
				&& SkyblockerConfigManager.get().dungeons.puzzleSolvers.blazeSolver
				&& SkyblockerConfigManager.get().dungeons.puzzleSolvers.blockIncorrectClicks;
	}

	/**
	 * Blocks drawing a bow unless the aim is on the blaze to kill next, so the puzzle can't be failed.
	 */
	private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		if (!shouldBlockWrongShots() || !(player.getItemInHand(hand).getItem() instanceof ProjectileWeaponItem)) {
			return InteractionResult.PASS;
		}

		ArmorStand target = getCurrentTargetBlaze();
		if (target == null) return InteractionResult.PASS;

		if (isAimingAtWrongBlaze(player, target)) {
			if (Debug.debugEnabled()) LOGGER.info("[Skyblocker Blaze] Blocked drawing a bow while aiming at the wrong blaze");
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.wrongBlaze");
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	/**
	 * Blocks attacking a blaze that isn't the one to kill next.
	 */
	private static InteractionResult onAttackEntity(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (!shouldBlockWrongShots()) return InteractionResult.PASS;

		ArmorStand target = getCurrentTargetBlaze();
		if (target == null || target == entity) return InteractionResult.PASS;

		if (isWrongBlaze(entity, target)) {
			if (Debug.debugEnabled()) LOGGER.info("[Skyblocker Blaze] Blocked an attack on the wrong blaze");
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.wrongBlaze");
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private static @Nullable ArmorStand getCurrentTargetBlaze() {
		if (highestBlaze == null || lowestBlaze == null || !highestBlaze.isAlive() || !lowestBlaze.isAlive()) return null;
		if (highestBlaze.getY() < 69) return highestBlaze;
		if (lowestBlaze.getY() > 69) return lowestBlaze;
		return null;
	}

	//do not trust the crosshair here, the puzzle blazes have no client hitboxes and it happily reports the pillar behind them
	//so we raycast our own boxes instead
	private static boolean isAimingAtWrongBlaze(Player player, ArmorStand target) {
		Vec3 from = player.getEyePosition();
		Vec3 to = from.add(player.getViewVector(1.0f).scale(64));

		double targetT = Double.MAX_VALUE;
		double otherT = Double.MAX_VALUE;
		for (ArmorStand stand : sortedBlazes) {
			if (!stand.isAlive()) continue;
			Vector2d t = new Vector2d();
			AABB box = blazeDetectionBox(stand);
			if (Intersectiond.intersectLineSegmentAab(from.x, from.y, from.z, to.x, to.y, to.z,
					box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, t) == Intersectiond.OUTSIDE) {
				continue;
			}
			if (stand == target) {
				targetT = Math.min(targetT, t.x);
			} else {
				otherT = Math.min(otherT, t.x);
			}
		}
		//only a wrong blaze hit before the target blocks, aiming at nothing can't fail anything
		return otherT < targetT;
	}

	private static boolean isWrongBlaze(Entity aimed, ArmorStand target) {
		ArmorStand nearest = null;
		double nearestDistSq = 9.0; //a blaze mob and its health display sit well within 3 blocks of each other
		for (ArmorStand stand : sortedBlazes) {
			double distSq = stand.distanceToSqr(aimed);
			if (distSq < nearestDistSq) {
				nearestDistSq = distSq;
				nearest = stand;
			}
		}
		return nearest != null && nearest != target;
	}

	//generous on purpose: covers the blaze plus wherever the health display happens to float
	//small boxes are how edge aims escaped. when in doubt, block
	private static AABB blazeDetectionBox(ArmorStand blaze) {
		Vec3 pos = blaze.position();
		return new AABB(pos.x - 1.0, pos.y - 2.5, pos.z - 1.0, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0);
	}

	private static AABB blazeBox(ArmorStand blaze) {
		return blaze.getBoundingBox().inflate(0.3, 0.9, 0.3).move(0, -1.1, 0);
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
		sortedBlazes = blazes.stream().map(ObjectIntPair::left).toList();
	}

	/**
	 * Retrieves Blaze entities in the world and parses their health information.
	 *
	 * @param world The client world to search for Blaze entities.
	 * @return A list of Blaze entities and their associated health.
	 */
	private static List<ObjectIntPair<ArmorStand>> getBlazesInWorld(ClientLevel world, LocalPlayer player) {
		List<ObjectIntPair<ArmorStand>> blazes = new ArrayList<>();
		for (ArmorStand blaze : world.getEntitiesOfClass(ArmorStand.class, player.getBoundingBox().inflate(500d), EntitySelector.ENTITY_NOT_BEING_RIDDEN)) {
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
			ArmorStand target = getCurrentTargetBlaze();
			if (target == null || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.blazeSolver) {
				return;
			}
			if (target == highestBlaze) {
				extractBlazeOutline(highestBlaze, nextHighestBlaze, collector);
			} else {
				extractBlazeOutline(lowestBlaze, nextLowestBlaze, collector);
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
		AABB blazeBox = blazeBox(blaze);
		collector.submitOutlinedBox(blazeBox, GREEN_COLOR_COMPONENTS, 5f, false);

		if (nextBlaze != null && nextBlaze.isAlive() && nextBlaze != blaze) {
			AABB nextBlazeBox = blazeBox(nextBlaze);
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
