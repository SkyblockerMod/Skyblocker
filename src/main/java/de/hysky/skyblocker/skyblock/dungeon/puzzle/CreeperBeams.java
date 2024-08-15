package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Intersectiond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private static BlockPos base = null;

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
    public void tick(MinecraftClient client) {

        // don't do anything if the room is solved
        if (!shouldSolve()) {
            return;
        }

        // clear state if not in dungeon
        if (client.world == null || client.player == null || !Utils.isInDungeons()) {
            return;
        }

        // try to find base if not found and solve
        if (base == null) {
            base = findCreeperBase(client.player, client.world);
            if (base == null) {
                return;
            }
            Vec3d creeperPos = new Vec3d(base.getX() + 0.5, BASE_Y + 1.75, base.getZ() + 0.5);
            ArrayList<BlockPos> targets = findTargets(client.world, base);
            beams = findLines(creeperPos, targets);
        }

        // update the beam states
        beams.forEach(b -> b.updateState(client.world));

        // check if the room is solved
        if (!isTarget(client.world, base)) {
            reset();
        }
    }

    // find the sea lantern block beneath the creeper
    private static BlockPos findCreeperBase(ClientPlayerEntity player, ClientWorld world) {

        // find all creepers
        List<CreeperEntity> creepers = world.getEntitiesByClass(
                CreeperEntity.class,
                player.getBoundingBox().expand(50D),
                EntityPredicates.VALID_ENTITY);

        if (creepers.isEmpty()) {
            return null;
        }

        // (sanity) check:
        // if the creeper isn't above a sea lantern, it's not the target.
        for (CreeperEntity ce : creepers) {
            Vec3d creeperPos = ce.getPos();
            BlockPos potentialBase = BlockPos.ofFloored(creeperPos.x, BASE_Y, creeperPos.z);
            if (isTarget(world, potentialBase)) {
                return potentialBase;
            }
        }

        return null;

    }

    // find the sea lanterns (and the ONE prismarine ty hypixel) in the room
    private static ArrayList<BlockPos> findTargets(ClientWorld world, BlockPos basePos) {
        ArrayList<BlockPos> targets = new ArrayList<>();

        BlockPos start = new BlockPos(basePos.getX() - 15, BASE_Y + 12, basePos.getZ() - 15);
        BlockPos end = new BlockPos(basePos.getX() + 16, FLOOR_Y, basePos.getZ() + 16);

        for (BlockPos pos : BlockPos.iterate(start, end)) {
            if (isTarget(world, pos)) {
                targets.add(new BlockPos(pos));
            }
        }
        return targets;
    }

    // generate lines between targets and finally find the solution
    private static ArrayList<Beam> findLines(Vec3d creeperPos, ArrayList<BlockPos> targets) {

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
    public void render(WorldRenderContext wrc) {

        // don't render if solved or disabled
        if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.creeperSolver) {
            return;
        }

        // lines.size() is always <= 4 so no issues OOB issues with the colors here.
        for (int i = 0; i < beams.size(); i++) {
            beams.get(i).render(wrc, COLORS[i]);
        }
    }

    private static boolean isTarget(ClientWorld world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.SEA_LANTERN || block == Blocks.PRISMARINE;
    }

    // helper class to hold all the things needed to render a beam
    private static class Beam {

        // raw block pos of target
        public BlockPos blockOne;
        public BlockPos blockTwo;

        // middle of targets used for rendering the line
        public Vec3d[] line = new Vec3d[2];

        // boxes used for rendering the block outline
        public Box outlineOne;
        public Box outlineTwo;

        // state: is this beam created/inputted or not?
        private boolean toDo = true;

        public Beam(BlockPos a, BlockPos b) {
            blockOne = a;
            blockTwo = b;
            line[0] = new Vec3d(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5);
            line[1] = new Vec3d(b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5);
            outlineOne = new Box(a);
            outlineTwo = new Box(b);
        }

        // used to filter the list of all beams so that no two beams share a target
        public boolean containsComponentOf(Beam other) {
            return this.blockOne.equals(other.blockOne)
                    || this.blockOne.equals(other.blockTwo)
                    || this.blockTwo.equals(other.blockOne)
                    || this.blockTwo.equals(other.blockTwo);
        }

        // update the state: is the beam created or not?
        public void updateState(ClientWorld world) {
            toDo = !(world.getBlockState(blockOne).getBlock() == Blocks.PRISMARINE
                    && world.getBlockState(blockTwo).getBlock() == Blocks.PRISMARINE);
        }

        // render either in a color if not created or faintly green if created
        public void render(WorldRenderContext wrc, float[] color) {
            if (toDo) {
                RenderHelper.renderOutline(wrc, outlineOne, color, 3, false);
                RenderHelper.renderOutline(wrc, outlineTwo, color, 3, false);
                RenderHelper.renderLinesFromPoints(wrc, line, color, 1, 2, false);
            } else {
                RenderHelper.renderOutline(wrc, outlineOne, GREEN_COLOR_COMPONENTS, 1, false);
                RenderHelper.renderOutline(wrc, outlineTwo, GREEN_COLOR_COMPONENTS, 1, false);
                RenderHelper.renderLinesFromPoints(wrc, line, GREEN_COLOR_COMPONENTS, 0.75f, 1, false);
            }
        }
    }
}
