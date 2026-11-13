package de.hysky.skyblocker.skyblock.dungeon.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.Command;
import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Click blocking and local rotation tracking approach adapted from
 * <a href="https://github.com/Noamm9/NoammAddons">NoammAddons</a> (CC0-1.0).
 */
public class ArrowAlign {
	private static final BlockPos LEFT_TOP = new BlockPos(-2, 124, 79);
	private static final AABB FRAMES_AREA = AABB.encapsulatingFullBlocks(LEFT_TOP, new BlockPos(-3, 120, 75));
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Arrow Align Solver");
	//The server's rotation update for a click lags behind the click itself, so a frame clicked within this window
	//keeps its locally tracked rotation instead of being re-read; one second is far beyond any normal lag
	private static final long RESYNC_DELAY_MS = 1000;

	private static int @Nullable [] solution = null;
	private static boolean noSolution = false;
	private static boolean inDeviceRange = false;
	//Rotations tracked locally per solution index, -1 when the frame hasn't been seen yet
	private static final int[] trackedRotations = new int[25];
	private static final long[] lastLocalClicks = new long[25];

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> reset());
		LevelRenderExtractionCallback.EVENT.register(ArrowAlign::extractRendering);
		UseEntityCallback.EVENT.register(ArrowAlign::onEntityInteract);
		AttackEntityCallback.EVENT.register(ArrowAlign::onAttackEntity);
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
		return handleRotate(player, frame, "entity click");
	}

	//mistakes happen sometimes ig? do we even need this? i will keep it here anyways just incase lol
	private static InteractionResult onAttackEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
		if (!(entity instanceof ItemFrame frame) || !FRAMES_AREA.contains(Vec3.atLowerCornerOf(frame.blockPosition()))) {
			return InteractionResult.PASS;
		}
		if (!shouldProcess() || Utils.isBlockerBypassed() || !SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks) return InteractionResult.PASS;

		debug(String.format("attack on frame %s -> BLOCKED (breaking it would fail the device)", frame.blockPosition().toShortString()));
		Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.breakBlocked");
		return InteractionResult.FAIL;
	}

	private static InteractionResult handleRotate(Player player, ItemFrame frame, String source) {
		BlockPos framePos = frame.blockPosition();

		if (!shouldProcess() || Utils.isBlockerBypassed() || !SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks || solution == null || noSolution) {
			debug(String.format("%s on frame %s IGNORED: solver %s, blocker %s, solution %s",
					source, framePos.toShortString(), shouldProcess(), SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks,
					solution == null ? (noSolution ? "none found" : "not computed") : "ready"));
			return InteractionResult.PASS;
		}

		int index = getSolutionIndex(framePos);
		int expected = solution[index];
		boolean sneak = player.isShiftKeyDown();

		//Negative values are the wool start/end markers and missing frames, none of them may be clicked
		if (expected < 0) {
			debug(String.format("%s: frame %s holds a wool marker, BLOCKED (sneak %s)", source, framePos.toShortString(), sneak));
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.frameDoesNotNeedRotating");
			return InteractionResult.FAIL;
		}

		int tracked = trackedRotations[index];
		if (tracked < 0) {
			debug(String.format("%s: frame %s rotation not seen yet, allowed (sneak %s)", source, framePos.toShortString(), sneak));
			return InteractionResult.PASS;
		}

		int remaining = (expected - tracked + 8) % 8;
		if (remaining == 0) {
			debug(String.format("%s: frame %s is already at its target rotation %d, BLOCKED (sneak %s)", source, framePos.toShortString(), tracked, sneak));
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.frameDoesNotNeedRotating");
			return InteractionResult.FAIL;
		}

		//Count the rotation locally right away, the server's update would arrive too late and waiting for it blocked fast clicks
		trackedRotations[index] = (tracked + 1) % 8;
		lastLocalClicks[index] = System.currentTimeMillis();
		debug(String.format("%s: frame %s rotation %d -> %d, expected %d, %d clicks left (sneak %s)",
				source, framePos.toShortString(), tracked, trackedRotations[index], expected, remaining - 1, sneak));
		return InteractionResult.PASS;
	}

	//sneak-clicks land on the block a frame hangs on instead of the frame, and hypixel still rotates it. blame the hitboxes
	private static InteractionResult onBlockInteract(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos clicked = hitResult.getBlockPos();
		if (!FRAMES_AREA.inflate(1).contains(Vec3.atLowerCornerOf(clicked))) {
			return InteractionResult.PASS;
		}

		boolean armed = shouldProcess() && !Utils.isBlockerBypassed() && SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks
				&& solution != null && !noSolution;
		if (!armed) {
			debug(String.format("block use: clicked %s IGNORED: solver disarmed (solution %s)", clicked.toShortString(),
					solution == null ? (noSolution ? "none found" : "not computed") : "ready"));
			return InteractionResult.PASS;
		}

		//a wall click rotates one of the adjacent frames, but which one is ambiguous, so it is never allowed
		ArrayList<String> adjacentDescriptions = new ArrayList<>();
		for (ItemFrame frame : getFrameEntitiesList()) {
			BlockPos framePos = frame.blockPosition();
			if (Math.abs(framePos.getX() - clicked.getX()) > 1 || Math.abs(framePos.getY() - clicked.getY()) > 1 || Math.abs(framePos.getZ() - clicked.getZ()) > 1) {
				continue;
			}
			adjacentDescriptions.add(String.format("%s rot %d exp %d", framePos.toShortString(), frame.getRotation(), solution[getSolutionIndex(framePos)]));
		}

		debug(String.format("block use: clicked %s -> BLOCKED (ambiguous frame), adjacent: %s", clicked.toShortString(),
				adjacentDescriptions.isEmpty() ? "none" : String.join(", ", adjacentDescriptions)));
		Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.frameDoesNotNeedRotating");
		return InteractionResult.FAIL;
	}

	/**
	 * Blocks shooting with a bow unless the aim is on an arrow that still needs rotating, so the device can't be failed.
	 */
	private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		if (!shouldProcess() || Utils.isBlockerBypassed() || !SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks
				|| solution == null || noSolution
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

		if (aimed == null) return InteractionResult.PASS;
		return handleRotate(player, aimed, "shot");
	}

	//the device can re-arm with a fresh layout, a solution whose wool and missing positions no longer match is stale
	private static boolean solutionMatchesFrames() {
		for (ItemFrame frame : getFrameEntitiesList()) {
			int expected = solution[getSolutionIndex(frame.blockPosition())];
			boolean wool = frame.getItem().is(Items.WOOL.lime()) || frame.getItem().is(Items.WOOL.red());
			if (expected == -1 || wool != (expected < -1)) {
				return false;
			}
		}
		return true;
	}

	//Re-reads every rotation the player hasn't clicked for a moment; this also picks up rotations caused by
	//other players and self-heals a local count that drifted from reality, e.g. after a shot that missed
	private static void syncRotations(List<ItemFrame> frames) {
		long now = System.currentTimeMillis();
		for (ItemFrame frame : frames) {
			int index = getSolutionIndex(frame.blockPosition());
			if (now - lastLocalClicks[index] < RESYNC_DELAY_MS) continue;

			int server = frame.getRotation();
			if (trackedRotations[index] != server) {
				debug(String.format("frame %s resynced: tracked %d, server %d", frame.blockPosition().toShortString(), trackedRotations[index], server));
				trackedRotations[index] = server;
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		boolean inRange = shouldProcess() && Minecraft.getInstance().player.position().distanceToSqr(FRAMES_AREA.getCenter()) < 64;
		if (inRange != inDeviceRange) {
			inDeviceRange = inRange;
			debug(inRange ? "entered device range" : "left device range, resetting state");
			if (!inRange) reset();
		}
		if (!inRange) {
			return;
		}

		if (solution != null && !solutionMatchesFrames()) {
			debug("solution is stale, re-deriving from the current frames");
			findSolution();
		} else if (solution == null && !noSolution) {
			debug("no solution yet, deriving from the current frames");
			findSolution();
		}

		List<ItemFrame> frames = getFrameEntitiesList();
		syncRotations(frames);

		if (!noSolution && solution != null) {
			for (ItemFrame frameEntity : frames) {
				int index = getSolutionIndex(frameEntity.blockPosition());
				int expect = solution[index];
				int tracked = trackedRotations[index];
				if (expect >= 0 && tracked >= 0) {
					int remaining = (expect + 8 - tracked) % 8;
					if (remaining > 0) {
						collector.submitText(Component.literal(String.valueOf(remaining)).withColor(ColorUtils.interpolate(0xFF00FF00, 0xFFFF0000, remaining / 7d)), frameEntity.position().add(0.3, 0, 0), false);
					}
				}
			}
		}
	}

	private static List<ItemFrame> getFrameEntitiesList() {
		return Minecraft.getInstance().level.getEntitiesOfClass(ItemFrame.class, FRAMES_AREA, _ -> true);
	}

	private static int getSolutionIndex(BlockPos pos) {
		return (LEFT_TOP.getY() - pos.getY()) * 5 + LEFT_TOP.getZ() - pos.getZ();
	}

	private static void findSolution() {
		List<ItemFrame> frameEntitiesList = getFrameEntitiesList();

		Optional<int[]> found = Align.SOLUTIONS.stream()
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

		solution = found.orElse(null);
		noSolution = found.isEmpty();
		if (noSolution) {
			LOGGER.error("[Skyblocker Arrow Align] Failed to find a solution for Arrow Align device!");
		} else {
			ArrayList<String> expectations = new ArrayList<>();
			for (ItemFrame frame : frameEntitiesList) {
				expectations.add(String.format("%s rot %d -> exp %d", frame.blockPosition().toShortString(), frame.getRotation(), solution[getSolutionIndex(frame.blockPosition())]));
			}
			debug(String.format("solution computed: %s", String.join("; ", expectations)));
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveArrowAlign &&
				Utils.isInDungeons() && BossPhase.isActive();
	}

	private static void reset() {
		if (solution != null || noSolution) {
			debug("state reset");
		}
		solution = null;
		noSolution = false;
		Arrays.fill(trackedRotations, -1);
		Arrays.fill(lastLocalClicks, 0);
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
