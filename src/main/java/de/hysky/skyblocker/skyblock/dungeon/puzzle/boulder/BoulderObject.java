package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import net.minecraft.util.math.BlockPos;

public record BoulderObject(int x, int y, int z, String type) {
	public BlockPos get3DPosition() {
		return new BlockPos(x, y, z);
	}
}
