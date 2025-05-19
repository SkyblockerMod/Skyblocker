package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public class Waterboard {
	public static final int BOARD_MIN_X = 6;
	public static final int BOARD_MAX_X = 24;
	public static final int BOARD_MIN_Y = 58;
	public static final int BOARD_MAX_Y = 81;
	public static final int BOARD_Z = 26;
	public static final BlockPos FIRST_SWITCH_POSITION = new BlockPos(15, 78, 26);

	public enum LeverType {
		COAL(Blocks.COAL_BLOCK, new BlockPos(20, 61, 10), DyeColor.RED),
		GOLD(Blocks.GOLD_BLOCK, new BlockPos(20, 61, 15), DyeColor.YELLOW),
		QUARTZ(Blocks.QUARTZ_BLOCK, new BlockPos(20, 61, 20), DyeColor.LIGHT_GRAY),
		DIAMOND(Blocks.DIAMOND_BLOCK, new BlockPos(10, 61, 20), DyeColor.CYAN),
		EMERALD(Blocks.EMERALD_BLOCK, new BlockPos(10, 61, 15), DyeColor.LIME),
		TERRACOTTA(Blocks.TERRACOTTA, new BlockPos(10, 61, 10), DyeColor.ORANGE),
		WATER(Blocks.LAVA, new BlockPos(15, 60, 5), DyeColor.LIGHT_BLUE);

		public final Block block;
		public final BlockPos leverPos;
		public final DyeColor color;

		LeverType(Block block, BlockPos leverPos, DyeColor color) {
			this.block = block;
			this.leverPos = leverPos;
			this.color = color;
		}

		public static LeverType fromName(String name) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.name().equalsIgnoreCase(name)) {
					return leverType;
				}
			}
			return null;
		}

		public static LeverType fromBlock(Block block) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.block == block) {
					return leverType;
				}
			}
			return null;
		}

		public static LeverType fromPos(BlockPos leverPos) {
			for (LeverType leverType : LeverType.values()) {
				if (leverPos.equals(leverType.leverPos)) {
					return leverType;
				}
			}
			return null;
		}
	}

}
