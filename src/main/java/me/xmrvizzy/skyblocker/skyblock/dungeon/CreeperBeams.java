package me.xmrvizzy.skyblocker.skyblock.dungeon;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Intersectiond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.architectury.event.events.client.ClientTooltipEvent.Render;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import me.xmrvizzy.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CreeperBeams {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreeperBeams.class.getName());

    // "missing, this palette looks like you stole it from a 2018 bootstrap webapp!"
    private static final float[][] COLORS = {
            { (float) 0xC6 / 0xFF, (float) 0xA1 / 0xFF, (float) 0x5B / 0xFF },
            { (float) 0x6D / 0xFF, (float) 0x59 / 0xFF, (float) 0x7A / 0xFF },
            { (float) 0xB5 / 0xFF, (float) 0x65 / 0xFF, (float) 0x76 / 0xFF },
            { (float) 0xE5 / 0xFF, (float) 0x6B / 0xFF, (float) 0x6F / 0xFF },
    };

    private static final int FLOOR_Y = 68;
    private static final int BASE_Y = 74;

    private static ArrayList<Vec3d[]> lines = new ArrayList<>();

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(CreeperBeams::update, 20);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(CreeperBeams::render);
    }

    private static void update() {

        if (!Utils.isInDungeons()) {
            lines.clear();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;

        if (world == null || player == null) {
            return;
        }

        if (lines.size() == 0) {

            BlockPos basePos = findCreeperBase(player, world);

            if (basePos == null) {
                return;
            }

            ArrayList<Vec3d> targets = findTargets(player, world, basePos);
            Vec3d creeperPos = new Vec3d(basePos.getX() + 0.5, BASE_Y + 3.5, basePos.getZ() + 0.5);

            lines = findLines(player, world, creeperPos, targets);
        }

    }

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
            Block block = world.getBlockState(potentialBase).getBlock();
            if (block == Blocks.SEA_LANTERN || block == Blocks.PRISMARINE) {
                player.sendMessage(Text.of(String.format("Base found at %s", potentialBase.toString())));
                return potentialBase;
            }
        }

        player.sendMessage(Text.of("Base not found"));
        return null;

    }

    // search for sea lanterns (and the ONE prismarine ty hypixel) and calculate
    // solutions
    private static ArrayList<Vec3d> findTargets(ClientPlayerEntity player, ClientWorld world, BlockPos basePos) {
        ArrayList<Vec3d> targets = new ArrayList<>();

        BlockPos start = new BlockPos(basePos.getX() - 15, BASE_Y + 12, basePos.getZ() - 15);
        BlockPos end = new BlockPos(basePos.getX() + 16, FLOOR_Y, basePos.getZ() + 16);

        for (BlockPos bp : BlockPos.iterate(start, end)) {
            Block b = world.getBlockState(bp).getBlock();
            if (b == Blocks.SEA_LANTERN || b == Blocks.PRISMARINE) {
                targets.add(new Vec3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5));
                player.sendMessage(Text.of(String.format("Found a target at %s", bp.toString())));
            }
        }
        return targets;
    }

    private static ArrayList<Vec3d[]> findLines(ClientPlayerEntity player, ClientWorld world, Vec3d creeperPos,
            ArrayList<Vec3d> targets) {

        ArrayList<Triple<Double, Integer, Integer>> allLines = new ArrayList<>();

        // optimize this a little bit by
        // only generating lines "one way", i.e. 1->2 but not 2->1
        for (int i = 0; i < targets.size(); i++) {
            for (int j = i + 1; j < targets.size(); j++) {
                Vec3d one = targets.get(i);
                Vec3d two = targets.get(j);
                double dist = Intersectiond.distancePointLine(
                        creeperPos.x, creeperPos.y, creeperPos.z,
                        one.x, one.y, one.z,
                        two.x, two.y, two.z);
                allLines.add(Triple.of(dist, i, j));
            }
        }

        // this still feels a bit heavy-handed, but it works for now.

        ArrayList<Vec3d[]> result = new ArrayList<>();

        allLines.sort((a, b) -> Double.compare(a.getLeft(), b.getLeft()));

        while (result.size() < 4 && !allLines.isEmpty()) {
            int idxA = allLines.get(0).getMiddle();
            int idxB = allLines.get(0).getRight();
            result.add(new Vec3d[] { targets.get(idxA), targets.get(idxB) });
            player.sendMessage(Text.of(String.format("Drawing line from %s to %s", targets.get(idxA).toString(),
                    targets.get(idxB).toString())));

            // remove the line we just added and other lines that use blocks we're using for
            // that line
            allLines.remove(0);
            allLines.removeIf(line -> line.getMiddle() == idxA
                    || line.getRight() == idxA
                    || line.getMiddle() == idxB
                    || line.getRight() == idxB);
        }

        if (result.size() != 4) {
            LOGGER.error("Not enough solutions found. This is bad...");
        }

        return result;
    }

    private static void render(WorldRenderContext wrc) {

        // lines.size() is always <= so no issues OOB issues here.
        for (int i = 0; i < lines.size(); i++) {
            Vec3d[] line = lines.get(i);
            RenderHelper.renderLinesFromPoints(wrc, line, COLORS[i], 1, 3);
        }
    }

}
