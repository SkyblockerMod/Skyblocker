package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.block.Block;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class OldLever {
    protected static final VoxelShape FLOOR_SHAPE;
    protected static final VoxelShape NORTH_SHAPE;
    protected static final VoxelShape SOUTH_SHAPE;
    protected static final VoxelShape EAST_SHAPE;
    protected static final VoxelShape WEST_SHAPE;

    public static VoxelShape getShape(WallMountLocation wallMountLocation, Direction direction) {
        if (!SkyblockerConfig.get().general.hitbox.oldLeverHitbox)
            return null;

        if (wallMountLocation == WallMountLocation.FLOOR) {
            return FLOOR_SHAPE;
        } else if (wallMountLocation == WallMountLocation.WALL) {
            switch (direction) {
                case EAST -> {
                    return EAST_SHAPE;
                }
                case WEST -> {
                    return WEST_SHAPE;
                }
                case SOUTH -> {
                    return SOUTH_SHAPE;
                }
                case NORTH -> {
                    return NORTH_SHAPE;
                }
            }
        }
        return null;
    }

    static {
        FLOOR_SHAPE = Block.createCuboidShape(4, 0, 4, 12, 10, 12);
        NORTH_SHAPE = Block.createCuboidShape(5.0D, 3.0D, 10.0D, 11.0D, 13.0D, 16.0D);
        SOUTH_SHAPE = Block.createCuboidShape(5.0D, 3.0D, 0.0D, 11.0D, 13.0D, 6.0D);
        WEST_SHAPE = Block.createCuboidShape(10.0D, 3.0D, 5.0D, 16.0D, 13.0D, 11.0D);
        EAST_SHAPE = Block.createCuboidShape(0.0D, 3.0D, 5.0D, 6.0D, 13.0D, 11.0D);
    }
}
