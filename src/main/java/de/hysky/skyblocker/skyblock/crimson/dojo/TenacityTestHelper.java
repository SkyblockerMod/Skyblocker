package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class TenacityTestHelper {


    private static final Map<ArmorStandEntity, List<Vec3d>> fireBallsWithStartPos = new HashMap<>();
    

    protected static void reset() {
        fireBallsWithStartPos.clear();
    }


    protected static void render(WorldRenderContext context) {
        for (ArmorStandEntity fireball : fireBallsWithStartPos.keySet()) {
            List<Vec3d> linePositions = fireBallsWithStartPos.get(fireball);
            Vec3d fireballPos = getCoalOffset(fireball.getPos());
            if (linePositions.getFirst().distanceTo(fireballPos) < 0.5 ) { //just spawned can not find line yet
                continue;
            }
            if (linePositions.size() < 2) {
                //calculate line for fireball and add it to its line values
                Vec3d distance = fireballPos.subtract(linePositions.getFirst()).multiply(100);
                Vec3d lineEnd = linePositions.getFirst().add(distance);
                linePositions.add(lineEnd);
            }
            RenderHelper.renderLinesFromPoints(context, new Vec3d[]{linePositions.get(0), linePositions.get(1)},new float[]{1f, 0f, 0f}, 1, 3, false);
            //could outline block to be broken but seems to have some random pattern so not usefull
        }

    }

    public static void onEntitySpawn(Entity entity) {
        if (entity instanceof ArmorStandEntity armorStand) {
            //todo they should be holding coal block but are not holding anything
            List<Vec3d> lineBlocks = new ArrayList<>();
            lineBlocks.add(getCoalOffset(armorStand.getPos()));
            fireBallsWithStartPos.put(armorStand,lineBlocks);
        }

    }

    public static void onEntityDespawn(Entity entity) {
        if (entity instanceof ArmorStandEntity armorStand) {
            fireBallsWithStartPos.remove(entity);
        }
    }

    private static Vec3d getCoalOffset(Vec3d pos) {
        return pos.add(0,1,0);
    }

}
