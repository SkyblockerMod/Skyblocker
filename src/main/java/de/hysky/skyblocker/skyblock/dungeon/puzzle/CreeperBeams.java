package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import org.joml.Intersectiond;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
	@SuppressWarnings("unused")
	private static final CreeperBeams INSTANCE = new CreeperBeams();

	private static ArrayList<Beam> beams = new ArrayList<>();
	private static @Nullable BlockPos base = null;

	private CreeperBeams() {
		super("creeper", "creeper-room");
	}

	@Init
	public static void init() {
	}

	@Override
	public void reset() {
		super.reset();
		beams.clear();
		base = null;
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

		// try to find base if not found and solve
		if (base == null) {
			base = findCreeperBase(client.player, client.level);
			if (base == null) {
				return;
			}
			Vec3 creeperPos = new Vec3(base.getX() + 0.5, BASE_Y + 1.75, base.getZ() + 0.5);
			ArrayList<BlockPos> targets = findTargets(client.level, base);
			beams = findLines(creeperPos, targets);
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
				player.getBoundingBox().inflate(50D),
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

	// generate lines between targets and finally find the solution
	private static ArrayList<Beam> findLines(Vec3 creeperPos, ArrayList<BlockPos> targets) {

		ArrayList<ObjectDoublePair<Beam>> allLines = new ArrayList<>();

		// optimize this a little bit by
		// only generating lines "one way", i.e. 1 -> 2 but not 2 -> 1
		for (int i = 0; i < targets.size(); i++) {
			for (int j = i + 1; j < targets.size(); j++) {
				Beam beam = new Beam(targets.get(i), targets.get(j));
				double dist = Intersectiond.distancePointLine(
						creeperPos.x, creeperPos.y, creeperPos.z,
						beam.line[0].x, beam.line[0].y, beam.line[0].z,
						beam.line[1].x, beam.line[1].y, beam.line[1].z);
				allLines.add(ObjectDoublePair.of(beam, dist));
			}
		}

		// this feels a bit heavy-handed, but it works for now.

		ArrayList<Beam> result = new ArrayList<>();
		allLines.sort(Comparator.comparingDouble(ObjectDoublePair::rightDouble));

		while (result.size() < 5 && !allLines.isEmpty()) {
			Beam solution = allLines.getFirst().left();
			result.add(solution);

			// remove the line we just added and other lines that use blocks we're using for
			// that line
			allLines.removeFirst();
			allLines.removeIf(beam -> solution.containsComponentOf(beam.left()));
		}

		if (result.size() < 5) {
			LOGGER.error("Not enough solutions found. This is bad...");
		}

		return result;
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {

		// don't render if solved or disabled
		if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperSolver) {
			return;
		}

		// lines.size() is always <= 4 so no issues OOB issues with the colors here.
		for (int i = 0; i < beams.size(); i++) {
			beams.get(i).extractRendering(collector, COLORS[i]);
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
