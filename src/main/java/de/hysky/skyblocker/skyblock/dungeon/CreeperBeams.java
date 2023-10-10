package de.hysky.skyblocker.skyblock.dungeon;

import java.util.ArrayList;
import java.util.List;

import org.joml.Intersectiond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CreeperBeams {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreeperBeams.class.getName());

    // "missing, this palette looks like you stole it from a 2018 bootstrap webapp!"
    private static final float[][] COLORS = {
            { 0.33f, 1f, 1f },
            { 1f, 0.33f, 0.33f },
            { 1f, 0.66f, 0f },
            { 1f, 0.33f, 1f },
    };

    private static final int FLOOR_Y = 68;
    private static final int BASE_Y = 74;

    private static ArrayList<Beam> beams = new ArrayList<>();
    private static BlockPos base = null;
    private static boolean solved = false;

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(CreeperBeams::update, 20);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(CreeperBeams::render);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> reset()));
    }

    private static void reset() {
        beams.clear();
        base = null;
        solved = false;
    }

    private static void update() {

        // don't do anything if the room is solved
        if (solved) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;

        // clear state if not in dungeon
        if (world == null || player == null || !Utils.isInDungeons()) {
            return;
        }

        // try to find base if not found and solve
        if (base == null) {
            base = findCreeperBase(player, world);
            if (base == null) {
                return;
            }
            Vec3d creeperPos = new Vec3d(base.getX() + 0.5, BASE_Y + 3.5, base.getZ() + 0.5);
            ArrayList<BlockPos> targets = findTargets(player, world, base);
            beams = findLines(player, world, creeperPos, targets);
        }

        // update the beam states
        beams.forEach(b -> b.updateState(world));

        // check if the room is solved
        if (!isTarget(world, base)) {
            solved = true;
        }
    }

    // find the sea lantern block beneath the creeper
    private static BlockPos findCreeperBase(ClientPlayerEntity player, ClientWorld world) {

        // find all creepers
        List<CreeperEntity> creepers = world.getEntitiesByClass(
                CreeperEntity.class,
                player.getBoundingBox().expand(50D),
                EntityPredicates.VALID_ENTITY);

        if (creepers.size() == 0) {
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
    private static ArrayList<BlockPos> findTargets(ClientPlayerEntity player, ClientWorld world, BlockPos basePos) {
        ArrayList<BlockPos> targets = new ArrayList<>();

        BlockPos start = new BlockPos(basePos.getX() - 15, BASE_Y + 12, basePos.getZ() - 15);
        BlockPos end = new BlockPos(basePos.getX() + 16, FLOOR_Y, basePos.getZ() + 16);

        for (BlockPos bp : BlockPos.iterate(start, end)) {
            if (isTarget(world, bp)) {
                targets.add(new BlockPos(bp));
            }
        }
        return targets;
    }

    // generate lines between targets and finally find the solution
    private static ArrayList<Beam> findLines(ClientPlayerEntity player, ClientWorld world, Vec3d creeperPos,
            ArrayList<BlockPos> targets) {

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
        allLines.sort((a, b) -> Double.compare(a.rightDouble(), b.rightDouble()));

        while (result.size() < 4 && !allLines.isEmpty()) {
            Beam solution = allLines.get(0).left();
            result.add(solution);

            // remove the line we just added and other lines that use blocks we're using for
            // that line
            allLines.remove(0);
            allLines.removeIf(beam -> solution.containsComponentOf(beam.left()));
        }

        if (result.size() != 4) {
            LOGGER.error("Not enough solutions found. This is bad...");
        }

        return result;
    }

    private static void render(WorldRenderContext wrc) {

        // don't render if solved
        if (solved) {
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
                RenderHelper.renderOutline(wrc, outlineOne, color, 3);
                RenderHelper.renderOutline(wrc, outlineTwo, color, 3);
                RenderHelper.renderLinesFromPoints(wrc, line, color, 1, 2);
            } else {
                RenderHelper.renderOutline(wrc, outlineOne, new float[] { 0.33f, 1f, 0.33f }, 1);
                RenderHelper.renderOutline(wrc, outlineTwo, new float[] { 0.33f, 1f, 0.33f }, 1);
                RenderHelper.renderLinesFromPoints(wrc, line, new float[] { 0.33f, 1f, 0.33f }, 0.75f, 1);
            }
        }
    }
}
