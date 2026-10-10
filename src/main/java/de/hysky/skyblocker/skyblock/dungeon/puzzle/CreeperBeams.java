package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

public class CreeperBeams extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreeperBeams.class.getName());

	private static final float[][] COLORS = {
			ColorUtils.getFloatComponents(DyeColor.LIGHT_BLUE),
			ColorUtils.getFloatComponents(DyeColor.LIME),
			ColorUtils.getFloatComponents(DyeColor.YELLOW),
			ColorUtils.getFloatComponents(DyeColor.MAGENTA),
			ColorUtils.getFloatComponents(DyeColor.PINK),
	};
	private static final float[] GREEN_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.GREEN);

	private static final int FLOOR_Y = 68;
	private static final int BASE_Y = 74;
	private static final int REQUIRED_HITS = 5;
	@SuppressWarnings("unused")
	private static final CreeperBeams INSTANCE = new CreeperBeams();

	private static ArrayList<Beam> beams = new ArrayList<>();
	private static ArrayList<BlockPos> targets = new ArrayList<>();
	private static @Nullable BlockPos base = null;
	private static boolean solvedWithHittingOnly = false;
	private static int lastActiveCount = -1;

	private CreeperBeams() {
		super("creeper", "creeper-room");
	}

	@Init
	public static void init() {
		UseItemCallback.EVENT.register(CreeperBeams::onUseItem);
	}

	private static boolean shouldBlockWrongShots() {
		return INSTANCE.shouldSolve()
				&& SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperSolver
				&& SkyblockerConfigManager.get().dungeons.puzzleSolvers.blockIncorrectClicks;
	}

	/**
	 * Blocks drawing a bow unless the aim is on a target the plan needs shot, so lanterns can't be wasted.
	 */
	private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		if (!shouldBlockWrongShots() || beams.isEmpty() || !(player.getItemInHand(hand).getItem() instanceof ProjectileWeaponItem)) {
			return InteractionResult.PASS;
		}

		Vec3 from = player.getEyePosition();
		Vec3 to = from.add(player.getViewVector(1.0f).scale(64));

		//find the first target block the aim passes through
		BlockPos aimed = null;
		double bestT = Double.MAX_VALUE;
		for (BlockPos target : targets) {
			AABB box = new AABB(target).inflate(0.25);
			Vector2d t = new Vector2d();
			if (Intersectiond.intersectLineSegmentAab(from.x, from.y, from.z, to.x, to.y, to.z,
					box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, t) == Intersectiond.OUTSIDE) {
				continue;
			}
			if (t.x < bestT) {
				bestT = t.x;
				aimed = target;
			}
		}
		//not aiming at any target can't waste one
		if (aimed == null) return InteractionResult.PASS;

		if (!isNeededTarget(aimed)) {
			if (Debug.debugEnabled()) LOGGER.info("[Skyblocker Creeper Beams] Blocked a shot at the target at {} that the plan does not need", aimed.toShortString());
			Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.wrongTarget");
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	//a target is needed when it belongs to a beam the plan still wants shot, with one beam at a time only the shown beam counts
	private static boolean isNeededTarget(BlockPos target) {
		boolean oneAtATime = SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperOnlyHittingBeams
				&& SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperShowOneBeam;
		for (Beam beam : beams) {
			if (oneAtATime && !beam.toDo) continue;
			if (beam.blockOne.equals(target) || beam.blockTwo.equals(target)) return true;
			if (oneAtATime) return false;
		}
		return false;
	}

	@Override
	public void reset() {
		super.reset();
		beams.clear();
		targets.clear();
		base = null;
		lastActiveCount = -1;
	}

	@Override
	public void tick(Minecraft client) {

		// don't do anything if the room is solved
		if (!shouldSolve()) {
			return;
		}

		// clear state if not in dungeon
		if (client.level == null || client.player == null || !Utils.isInDungeons()) {
			return;
		}

		// recompute the solution when the toggle changes so it applies without re-entering the room
		if (base != null && SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperOnlyHittingBeams != solvedWithHittingOnly) {
			base = null;
		}

		// try to find base if not found
		if (base == null) {
			base = findCreeperBase(client.player, client.level);
			if (base == null) {
				return;
			}
			targets = findTargets(client.level, base);
			lastActiveCount = -1;
		}

		// resolve whenever the number of activated targets changes, so the plan adapts to beams the player made off-solution
		int activeCount = 0;
		for (BlockPos target : targets) {
			if (client.level.getBlockState(target).getBlock() == Blocks.PRISMARINE) activeCount++;
		}
		if (activeCount != lastActiveCount) {
			lastActiveCount = activeCount;
			solveBeams(client.level, base);
		}

		// update the beam states
		beams.forEach(b -> b.updateState(client.level));

		// check if the room is solved
		if (!isTarget(client.level, base)) {
			reset();
		}
	}

	// find the sea lantern block beneath the creeper
	private static @Nullable BlockPos findCreeperBase(LocalPlayer player, ClientLevel world) {

		// find all creepers
		List<Creeper> creepers = world.getEntitiesOfClass(
				Creeper.class,
				player.getBoundingBox().inflate(50d),
				EntitySelector.ENTITY_STILL_ALIVE);

		if (creepers.isEmpty()) {
			return null;
		}

		// (sanity) check:
		// if the creeper isn't above a sea lantern, it's not the target.
		for (Creeper ce : creepers) {
			Vec3 creeperPos = ce.position();
			BlockPos potentialBase = BlockPos.containing(creeperPos.x, BASE_Y, creeperPos.z);
			if (isTarget(world, potentialBase)) {
				return potentialBase;
			}
		}

		return null;

	}

	// find the sea lanterns (and the ONE prismarine ty hypixel) in the room
	private static ArrayList<BlockPos> findTargets(ClientLevel world, BlockPos basePos) {
		ArrayList<BlockPos> targets = new ArrayList<>();

		BlockPos start = new BlockPos(basePos.getX() - 15, BASE_Y + 12, basePos.getZ() - 15);
		BlockPos end = new BlockPos(basePos.getX() + 16, FLOOR_Y, basePos.getZ() + 16);

		for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
			if (isTarget(world, pos)) {
				targets.add(new BlockPos(pos));
			}
		}
		return targets;
	}

	private static void solveBeams(ClientLevel world, BlockPos base) {
		Vec3 creeperPos = new Vec3(base.getX() + 0.5, BASE_Y + 1.75, base.getZ() + 0.5);
		boolean onlyHitting = SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperOnlyHittingBeams;
		solvedWithHittingOnly = onlyHitting;

		ArrayList<BlockPos> active = new ArrayList<>();
		ArrayList<BlockPos> inactive = new ArrayList<>();
		for (BlockPos target : targets) {
			if (world.getBlockState(target).getBlock() == Blocks.PRISMARINE) active.add(target);
			else inactive.add(target);
		}

			//beams between already active targets count as hits too. yes, even the off-plan ones. players get creative
		int hits = 0;
		if (onlyHitting) {
			for (int i = 0; i < active.size(); i++) {
				for (int j = i + 1; j < active.size(); j++) {
					if (goesThroughCreeper(new Beam(active.get(i), active.get(j)), base)) hits++;
				}
			}
		}
		int needed = REQUIRED_HITS - hits;

		beams = new ArrayList<>();
		if (needed <= 0) {
			return;
		}

		ArrayList<ObjectDoublePair<Beam>> candidates = new ArrayList<>();

		// optimize this a little bit by
		// only generating lines "one way", i.e. 1 -> 2 but not 2 -> 1
		for (int i = 0; i < inactive.size(); i++) {
			for (int j = i + 1; j < inactive.size(); j++) {
				Beam beam = new Beam(inactive.get(i), inactive.get(j));
					if (onlyHitting && !goesThroughCreeper(beam, base)) {
					continue;
				}
				double dist = Intersectiond.distancePointLine(
						creeperPos.x, creeperPos.y, creeperPos.z,
						beam.line[0].x, beam.line[0].y, beam.line[0].z,
						beam.line[1].x, beam.line[1].y, beam.line[1].z);
				candidates.add(ObjectDoublePair.of(beam, dist));
			}
		}

		// this feels a bit heavy-handed, but it works for now.

		candidates.sort(Comparator.comparingDouble(ObjectDoublePair::rightDouble));
		int want = onlyHitting ? needed : REQUIRED_HITS;
		while (beams.size() < want && !candidates.isEmpty()) {
			Beam solution = candidates.getFirst().left();
			beams.add(solution);

			// remove the line we just added and other lines that use blocks we're using for
			// that line
			candidates.removeFirst();
			candidates.removeIf(beam -> solution.containsComponentOf(beam.left()));
		}

		if (beams.size() < want) {
			LOGGER.error("Not enough solutions found. This is bad...");
		}
	}

	//width is strict, a graze just wastes two targets. height is generous, hypixel counts hits anywhere
	//in the tall column the bobbing creeper occupies (~4 blocks), no matter where it currently floats
	//does the tower count? im not sure lol (cata 39 btw)
	private static boolean goesThroughCreeper(Beam beam, BlockPos base) {
		return Intersectiond.intersectLineSegmentAab(
				beam.line[0].x, beam.line[0].y, beam.line[0].z,
				beam.line[1].x, beam.line[1].y, beam.line[1].z,
				base.getX() + 0.2, BASE_Y + 1, base.getZ() + 0.2,
				base.getX() + 0.8, BASE_Y + 5, base.getZ() + 0.8,
				new Vector2d()) != Intersectiond.OUTSIDE;
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {

		// don't render if solved or disabled
		if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperSolver) {
			return;
		}

		// only show the next beam to make instead of all of them
		boolean oneAtATime = SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperOnlyHittingBeams
				&& SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperShowOneBeam;

		// lines.size() is always <= 4 so no issues OOB issues with the colors here.
		for (int i = 0; i < beams.size(); i++) {
			Beam beam = beams.get(i);
			if (oneAtATime && !beam.toDo) continue;
			beam.extractRendering(collector, COLORS[i]);
			if (oneAtATime) break;
		}
	}

	private static boolean isTarget(ClientLevel world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		return block == Blocks.SEA_LANTERN || block == Blocks.PRISMARINE;
	}

	// helper class to hold all the things needed to render a beam
	private static class Beam {

		// raw block pos of target
		private final BlockPos blockOne;
		private final BlockPos blockTwo;

		// middle of targets used for rendering the line
		private final Vec3[] line = new Vec3[2];

		// boxes used for rendering the block outline
		private final AABB outlineOne;
		private final AABB outlineTwo;

		// state: is this beam created/inputted or not?
		private boolean toDo = true;

		private Beam(BlockPos a, BlockPos b) {
			blockOne = a;
			blockTwo = b;
			line[0] = new Vec3(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5);
			line[1] = new Vec3(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5);
			outlineOne = new AABB(a);
			outlineTwo = new AABB(b);
		}

		// used to filter the list of all beams so that no two beams share a target
		private boolean containsComponentOf(Beam other) {
			return this.blockOne.equals(other.blockOne)
					|| this.blockOne.equals(other.blockTwo)
					|| this.blockTwo.equals(other.blockOne)
					|| this.blockTwo.equals(other.blockTwo);
		}

		// update the state: is the beam created or not?
		private void updateState(ClientLevel world) {
			toDo = !(world.getBlockState(blockOne).getBlock() == Blocks.PRISMARINE
					&& world.getBlockState(blockTwo).getBlock() == Blocks.PRISMARINE);
		}

		// render either in a color if not created or faintly green if created
		private void extractRendering(PrimitiveCollector collector, float[] color) {
			if (toDo) {
				collector.submitOutlinedBox(outlineOne, color, 3, false);
				collector.submitOutlinedBox(outlineTwo, color, 3, false);
				collector.submitLinesFromPoints(line, color, 1, 2, false);
			} else {
				collector.submitOutlinedBox(outlineOne, GREEN_COLOR_COMPONENTS, 1, false);
				collector.submitOutlinedBox(outlineTwo, GREEN_COLOR_COMPONENTS, 1, false);
				collector.submitLinesFromPoints(line, GREEN_COLOR_COMPONENTS, 0.75f, 1, false);
			}
		}
	}
}
