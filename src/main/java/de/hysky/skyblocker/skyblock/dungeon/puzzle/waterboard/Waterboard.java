package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import java.util.Locale;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;

public class Waterboard {
	public static final int BOARD_MIN_X = 6;
	public static final int BOARD_MAX_X = 24;
	public static final int BOARD_MIN_Y = 58;
	public static final int BOARD_MAX_Y = 81;
	public static final int BOARD_Z = 26;
	// The top center of the grid, between the first two toggleable blocks
	public static final BlockPos WATER_ENTRANCE_POSITION = new BlockPos(15, 78, 26);

	public enum LeverType implements StringRepresentable {
		COAL(Blocks.COAL_BLOCK, new BlockPos(20, 61, 10), DyeColor.RED, new BlockPos[]{
				new BlockPos(0, -2, 0), new BlockPos(2, -1, 1),
				null, new BlockPos(5, -1, 0)
		}),
		GOLD(Blocks.GOLD_BLOCK, new BlockPos(20, 61, 15), DyeColor.YELLOW, new BlockPos[]{
				new BlockPos(1, -1, 0), new BlockPos(3, -2, 0),
				new BlockPos(-4, -1, 1), new BlockPos(1, 0, 0)
		}),
		QUARTZ(Blocks.QUARTZ_BLOCK, new BlockPos(20, 61, 20), DyeColor.LIGHT_GRAY, new BlockPos[]{
				new BlockPos(1, -4, 1), new BlockPos(-1, 0, 0),
				new BlockPos(1, 0, 0), new BlockPos(-1, 0, 1)
		}),
		DIAMOND(Blocks.DIAMOND_BLOCK, new BlockPos(10, 61, 20), DyeColor.CYAN, new BlockPos[]{
				new BlockPos(0, -5, 1), new BlockPos(-2, -1, 0),
				new BlockPos(-1, 0, 1), new BlockPos(-3, -4, 1)
		}),
		EMERALD(Blocks.EMERALD_BLOCK, new BlockPos(10, 61, 15), DyeColor.LIME, new BlockPos[]{
				new BlockPos(-1, -10, 1), new BlockPos(1, 0, 1),
				new BlockPos(-6, 0, 0), new BlockPos(1, -4, 0)
		}),
		TERRACOTTA(Blocks.TERRACOTTA, new BlockPos(10, 61, 10), DyeColor.ORANGE, new BlockPos[]{
				new BlockPos(-1, -1, 1), new BlockPos(0, -3, 1),
				null, new BlockPos(-4, -5, 1)
		}),
		WATER(Blocks.LAVA, new BlockPos(15, 60, 5), DyeColor.LIGHT_BLUE, null);

		private static final Codec<LeverType> CODEC = StringRepresentable.fromEnum(LeverType::values);

		public final Block block;
		public final BlockPos leverPos;
		public final DyeColor color;
		// Holds positions where the corresponding block is present in the initial state of each variant, offset from the water entrance position
		// This is more reliable at detecting if the lever is active than looking at the lever's block state
		public final @Nullable BlockPos[] initialPositions;

		LeverType(Block block, BlockPos leverPos, DyeColor color, BlockPos[] initialPositions) {
			this.block = block;
			this.leverPos = leverPos;
			this.color = color;
			this.initialPositions = initialPositions;
		}

		public static @Nullable LeverType fromName(String name) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.name().equalsIgnoreCase(name)) {
					return leverType;
				}
			}
			return null;
		}

		public static @Nullable LeverType fromBlock(Block block) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.block == block) {
					return leverType;
				}
			}
			return null;
		}

		public static @Nullable LeverType fromPos(BlockPos leverPos) {
			for (LeverType leverType : LeverType.values()) {
				if (leverPos.equals(leverType.leverPos)) {
					return leverType;
				}
			}
			return null;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		public static class LeverTypeArgumentType extends StringRepresentableArgument<LeverType> {
			private LeverTypeArgumentType() {
				super(CODEC, LeverType::values);
			}

			public static LeverTypeArgumentType leverType() {
				return new LeverTypeArgumentType();
			}

			public static <S> LeverType getLeverType(CommandContext<S> context, String name) {
				return context.getArgument(name, LeverType.class);
			}
		}
	}
}
