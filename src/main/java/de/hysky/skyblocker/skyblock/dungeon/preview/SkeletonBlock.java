package de.hysky.skyblocker.skyblock.dungeon.preview;

// Simple data class to store all the block info.
public record SkeletonBlock(int x, int y, int z, byte blockType) {
	public static SkeletonBlock from(int blockNum) {
		return new SkeletonBlock(
				(blockNum >> 24) & 255,
				(blockNum >> 16) & 255,
				(blockNum >> 8) & 255,
				(byte) (blockNum & 255)
		);
	}

	public int compress() {
		return x << 24 | y << 16 | z << 8 | blockType;
	}
}
