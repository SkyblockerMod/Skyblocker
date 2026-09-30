package de.hysky.skyblocker.skyblock.dungeon.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.Command;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ArrowAlign {
	private static final BlockPos LEFT_TOP = new BlockPos(-2, 124, 79);
	private static final AABB FRAMES_AREA = AABB.encapsulatingFullBlocks(LEFT_TOP, new BlockPos(-3, 120, 75));
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Arrow Align Solver");

	private static int[] currentSolution = null;
	private static boolean noSolution = false;
	private static boolean inDeviceRange = false;
	private static final Map<String, Integer> lastKnownRotations = new HashMap<>();
	private static final Object2LongOpenHashMap<String> rotationConfirmationCooldowns = new Object2LongOpenHashMap<>();
	private static final long ROTATION_CONFIRMATION_COOLDOWN_MS = 400;

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> reset());
		LevelRenderExtractionCallback.EVENT.register(ArrowAlign::extractRendering);
		UseEntityCallback.EVENT.register(ArrowAlign::onEntityInteract);
		UseBlockCallback.EVENT.register(ArrowAlign::onBlockInteract);
		UseItemCallback.EVENT.register(ArrowAlign::onUseItem);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("device").then(literal("arrow-align")
				.then(literal("solve").executes(_ -> {
					findSolution();
					return Command.SINGLE_SUCCESS;
				}))
		)))));
	}

	private static void debug(String message) {
		if (Debug.debugEnabled()) LOGGER.info("[Skyblocker Arrow Align] {}", message);
	}

	private static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
		if (!(entity instanceof ItemFrame frame) || !FRAMES_AREA.contains(Vec3.atLowerCornerOf(frame.blockPosition()))) {
			return InteractionResult.PASS;
		}
		BlockPos framePos = frame.blockPosition();

		if (!shouldProcess() || !SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks || currentSolution == null || noSolution) {
			debug(String.format("entity click on frame %s IGNORED: solver %s, blocker %s, solution %s",
					framePos.toShortString(), shouldProcess(), SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks,
					currentSolution == null ? (noSolution ? "none found" : "not computed") : "ready"));
			return InteractionResult.PASS;
		}

		int expected = currentSolution[getSolutionIndex(framePos)];

		//Negative values are the wool start/end markers and arrows already at their target rotation must not be clicked
		boolean blocked = expected < 0 || frame.getRotation() == expected;

		if (!blocked && isAwaitingRotationConfirmation(frame)) {
			debug(String.format("entity click: frame %s rotation %d expected %d -> BLOCKED, waiting for the server to confirm the previous rotation", framePos.toShortString(), frame.getRotation(), expected));
			return InteractionResult.FAIL;
		}

		debug(String.format("entity click: frame %s rotation %d expected %d -> %s", framePos.toShortString(), frame.getRotation(), expected, blocked ? "BLOCKED" : "allowed"));
		if (blocked) {
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.frameDoesNotNeedRotating");
			return InteractionResult.FAIL;
		}
		markAwaitingRotationConfirmation(frame);
		return InteractionResult.PASS;
	}

	//sneak-clicks land on the block a frame hangs on instead of the frame, and hypixel still rotates it. blame the hitboxes
	private static InteractionResult onBlockInteract(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos clicked = hitResult.getBlockPos();
		if (!FRAMES_AREA.inflate(1).contains(Vec3.atLowerCornerOf(clicked))) {
			return InteractionResult.PASS;
		}

		boolean armed = shouldProcess() && SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks
				&& currentSolution != null && !noSolution;

		//a wall click rotates one of the adjacent frames, but which one is ambiguous, so it is never allowed
		boolean blocked = true;
		ArrayList<String> adjacentDescriptions = new ArrayList<>();
		for (ItemFrame frame : getFrameEntitiesList()) {
			BlockPos framePos = frame.blockPosition();
			if (Math.abs(framePos.getX() - clicked.getX()) > 1 || Math.abs(framePos.getY() - clicked.getY()) > 1 || Math.abs(framePos.getZ() - clicked.getZ()) > 1) {
				continue;
			}
			adjacentDescriptions.add(String.format("%s rot %d exp %d", framePos.toShortString(), frame.getRotation(), currentSolution[getSolutionIndex(framePos)]));
		}
		String adjacent = adjacentDescriptions.isEmpty() ? "none" : String.join(", ", adjacentDescriptions);

		debug(String.format("block use: clicked %s -> %s%s", clicked.toShortString(), blocked ? "BLOCKED" : "allowed", armed ? ", adjacent: " + adjacent : " (disarmed)"));
		if (blocked) {
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.frameDoesNotNeedRotating");
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	/**
	 * Blocks shooting with a bow unless the aim is on an arrow that still needs rotating, so the device can't be failed.
	 */
	private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		if (!shouldProcess() || !SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks
				|| currentSolution == null || noSolution
				|| !(player.getItemInHand(hand).getItem() instanceof ProjectileWeaponItem)) {
			return InteractionResult.PASS;
		}

		Vec3 from = player.getEyePosition();
		Vec3 to = from.add(player.getViewVector(1.0f).scale(64));

		//find the first device frame the aim passes through
		ItemFrame aimed = null;
		double bestT = Double.MAX_VALUE;
		for (ItemFrame frame : getFrameEntitiesList()) {
			AABB box = frame.getBoundingBox().inflate(0.35);
			Vector2d t = new Vector2d();
			if (Intersectiond.intersectLineSegmentAab(from.x, from.y, from.z, to.x, to.y, to.z,
					box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, t) == Intersectiond.OUTSIDE) {
				continue;
			}
			if (t.x < bestT) {
				bestT = t.x;
				aimed = frame;
			}
		}

		if (aimed == null) {
			debug("shot: not aiming at any frame, allowed");
			return InteractionResult.PASS;
		}

		int expected = currentSolution[getSolutionIndex(aimed.blockPosition())];
		boolean blocked = expected < 0 || aimed.getRotation() == expected;

		if (!blocked && isAwaitingRotationConfirmation(aimed)) {
			debug(String.format("shot: frame %s rotation %d expected %d -> BLOCKED, waiting for the server to confirm the previous rotation", aimed.blockPosition().toShortString(), aimed.getRotation(), expected));
			return InteractionResult.FAIL;
		}

		debug(String.format("shot: frame %s rotation %d expected %d -> %s", aimed.blockPosition().toShortString(), aimed.getRotation(), expected, blocked ? "BLOCKED" : "allowed"));
		if (blocked) {
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.frameDoesNotNeedRotating");
			return InteractionResult.FAIL;
		}
		markAwaitingRotationConfirmation(aimed);
		return InteractionResult.PASS;
	}

	//the device can re-arm with a fresh layout, a solution whose wool and missing positions no longer match is stale
	private static boolean solutionMatchesFrames() {
		for (ItemFrame frame : getFrameEntitiesList()) {
			int expected = currentSolution[getSolutionIndex(frame.blockPosition())];
			boolean wool = frame.getItem().is(Items.WOOL.lime()) || frame.getItem().is(Items.WOOL.red());
			if (expected == -1 || wool != (expected < -1)) {
				return false;
			}
		}
		return true;
	}

	//the server confirms rotations a moment late, without this cooldown rapid clicks rotate straight past the target
	private static boolean isAwaitingRotationConfirmation(ItemFrame frame) {
		return System.currentTimeMillis() - rotationConfirmationCooldowns.getLong(frame.blockPosition().toShortString()) < ROTATION_CONFIRMATION_COOLDOWN_MS;
	}

	private static void markAwaitingRotationConfirmation(ItemFrame frame) {
		rotationConfirmationCooldowns.put(frame.blockPosition().toShortString(), System.currentTimeMillis());
	}

	private static void extractRendering(PrimitiveCollector collector) {
		boolean inRange = shouldProcess() && Minecraft.getInstance().player.position().distanceToSqr(FRAMES_AREA.getCenter()) < 64;
		if (inRange != inDeviceRange) {
			inDeviceRange = inRange;
			debug(inRange ? "entered device range" : "left device range");
		}
		if (!inRange) {
			return;
		}

		if (currentSolution != null && !solutionMatchesFrames()) {
			debug("solution is stale, re-deriving from the current frames");
			findSolution();
		} else if (currentSolution == null && !noSolution) {
			debug("no solution yet, deriving from the current frames");
			findSolution();
		}

		List<ItemFrame> frames = getFrameEntitiesList();
		logFrameChanges(frames);

		if (!noSolution) {
			for (ItemFrame frameEntity : frames) {
				int now = frameEntity.getRotation();
				int expect = currentSolution[getSolutionIndex(frameEntity.blockPosition())];
				if (expect >= 0) {
					int remaining = (expect + 8 - now) % 8;
					if (remaining > 0) {
						collector.submitText(Component.literal(String.valueOf(remaining)).withColor(ColorUtils.interpolate(0xFF00FF00, 0xFFFF0000, remaining / 7d)), frameEntity.position().add(0.3, 0, 0), false);
					}
				}
			}
		}
	}

	//every rotation change the client sees. if a blocked frame still rotates, a packet escaped. this is how we catch it
	private static void logFrameChanges(List<ItemFrame> frames) {
		ArrayList<String> currentKeys = new ArrayList<>();
		for (ItemFrame frame : frames) {
			String key = frame.blockPosition().toShortString();
			currentKeys.add(key);
			int rotation = frame.getRotation();
			Integer last = lastKnownRotations.get(key);
			if (last == null) {
				debug(String.format("frame %s appeared: rotation %d, item %s", key, rotation, describeItem(frame)));
			} else if (last != rotation) {
				rotationConfirmationCooldowns.removeLong(key);
				String expected = currentSolution != null ? String.valueOf(currentSolution[getSolutionIndex(frame.blockPosition())]) : "?";
				debug(String.format("frame %s ROTATED: %d -> %d (expected %s)", key, last, rotation, expected));
			}
			lastKnownRotations.put(key, rotation);
		}
		lastKnownRotations.keySet().removeIf(key -> !currentKeys.contains(key));
	}

	private static String describeItem(ItemFrame frame) {
		if (frame.getItem().is(Items.WOOL.lime())) return "wool start";
		if (frame.getItem().is(Items.WOOL.red())) return "wool end";
		if (frame.getItem().is(Items.ARROW)) return "arrow";
		return "other";
	}

	private static List<ItemFrame> getFrameEntitiesList() {
		return Minecraft.getInstance().level.getEntitiesOfClass(ItemFrame.class, FRAMES_AREA, _ -> true);
	}

	private static int getSolutionIndex(BlockPos pos) {
		return (LEFT_TOP.getY() - pos.getY()) * 5 + LEFT_TOP.getZ() - pos.getZ();
	}

	private static void findSolution() {
		List<ItemFrame> frameEntitiesList = getFrameEntitiesList();

		Optional<int[]> solution = Align.SOLUTIONS.stream()
				.filter(rotations -> {
					for (ItemFrame itemFrame : frameEntitiesList) {
						switch (rotations[getSolutionIndex(itemFrame.blockPosition())]) {
							case Align.X -> {
								return false;
							}
							case Align.S -> {
								if (!itemFrame.getItem().is(Items.WOOL.lime())) return false;
							}
							case Align.E -> {
								if (!itemFrame.getItem().is(Items.WOOL.red())) return false;
							}
							default -> {
								if (!itemFrame.getItem().is(Items.ARROW)) return false;
							}
						}
					}
					return true;
				})
				.findAny();

		currentSolution = solution.orElse(null);
		noSolution = solution.isEmpty();
		if (noSolution) {
			LOGGER.error("[Skyblocker Arrow Align] Failed to find a solution for Arrow Align device!");
		} else {
			ArrayList<String> expectations = new ArrayList<>();
			for (ItemFrame frame : frameEntitiesList) {
				expectations.add(String.format("%s rot %d -> exp %d", frame.blockPosition().toShortString(), frame.getRotation(), currentSolution[getSolutionIndex(frame.blockPosition())]));
			}
			debug(String.format("solution computed: %s", String.join("; ", expectations)));
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveArrowAlign &&
				Utils.isInDungeons() && DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR;
	}

	private static void reset() {
		if (currentSolution != null || noSolution) {
			debug("state reset");
		}
		currentSolution = null;
		noSolution = false;
		lastKnownRotations.clear();
		rotationConfirmationCooldowns.clear();
	}

	private static class Align {
		private static final int X = -1; // missing
		private static final int S = -2; // start
		private static final int E = -3; // end
		private static final int U = 7; // up
		private static final int D = 3; // down
		private static final int L = 5; // left
		private static final int R = 1; // right

		private static final List<int[]> SOLUTIONS = List.of(
				new int[]{
						R, R, D, X, X,
						U, X, D, X, X,
						S, X, D, X, E,
						X, X, D, X, U,
						X, X, R, R, U},
				new int[]{
						R, R, E, L, L,
						U, X, X, X, U,
						U, L, X, R, U,
						X, U, X, U, X,
						X, S, X, S, X},
				new int[]{
						D, L, L, X, X,
						D, X, U, X, X,
						E, X, S, X, E,
						X, X, D, X, U,
						X, X, R, R, U},
				new int[]{
						S, X, X, X, S,
						D, X, X, X, D,
						D, X, E, X, D,
						D, X, U, X, D,
						R, R, U, L, L},
				new int[]{
						R, R, R, R, D,
						U, X, X, X, D,
						U, X, E, X, D,
						U, X, U, X, D,
						S, X, U, L, L},
				new int[]{
						R, R, D, X, E,
						U, X, D, X, U,
						U, X, D, X, U,
						U, X, D, X, U,
						S, X, R, R, U},
				new int[]{
						S, R, R, R, E,
						X, X, X, X, X,
						S, R, R, R, E,
						X, X, X, X, X,
						S, R, R, R, E},
				new int[]{
						X, R, R, D, X,
						X, U, X, D, X,
						X, U, X, D, X,
						X, U, X, D, X,
						S, U, X, R, E},
				new int[]{
						S, R, D, X, X,
						X, X, R, R, E,
						S, R, U, X, X,
						X, X, R, R, E,
						S, R, U, X, X}
		);
	}
}
