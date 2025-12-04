package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class OldLever {
	protected static final VoxelShape FLOOR_SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D);
	protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.0D, 3.0D, 10.0D, 11.0D, 13.0D, 16.0D);
	protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.0D, 3.0D, 0.0D, 11.0D, 13.0D, 6.0D);
	protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0D, 3.0D, 5.0D, 6.0D, 13.0D, 11.0D);
	protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(10.0D, 3.0D, 5.0D, 16.0D, 13.0D, 11.0D);

	@SuppressWarnings("incomplete-switch")
	public static VoxelShape getShape(BlockFace face, Direction direction) {
		if (!SkyblockerConfigManager.get().general.hitbox.oldLeverHitbox)
			return null;

		if (face == BlockFace.FLOOR) {
			return FLOOR_SHAPE;
		} else if (face == BlockFace.WALL) {
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
}
