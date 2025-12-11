package de.hysky.skyblocker.skyblock.dungeon.roomPreview;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomStructure {
	static NbtCompound getCompound(int[] blocks) {
		return createCompound(createBlockList(blocks));
	}

	static List<SkeletonBlock> createBlockList(int[] blocks) {
		List<SkeletonBlock> blockData = new ArrayList<>(blocks.length);
		for (int blockNum : blocks) {
			blockData.add(new SkeletonBlock(
					(blockNum >> 24) & 255,
					(blockNum >> 16) & 255,
					(blockNum >> 8) & 255,
					(byte) (blockNum & 255)
			));
		}
		return blockData;
	}

	/**
	 * Manually creates a structure .nbt file from the skeleton blocks.
	 * A structure template is an NbtCompound of "size", "palette", and "blocks"
	 */
	static NbtCompound createCompound(List<SkeletonBlock> blockData) {
		NbtCompound structure = new NbtCompound();

		// Structure Size
		structure.put("size", getSize(blockData));

		// Palette
		NbtCompound[] sortedPalette = new NbtCompound[DungeonManager.NUMERIC_ID.size()];
		// The entries are not guaranteed to be ordered.
		for (var entry : DungeonManager.NUMERIC_ID.object2ByteEntrySet()) {
			NbtCompound paletteBlock = new NbtCompound();
			paletteBlock.put("Name", NbtString.of(entry.getKey()));
			sortedPalette[entry.getByteValue() - 1] = paletteBlock; // Subtract 1 because NUMERIC_ID starts at 1.
		}
		NbtList palette = new NbtList();
		palette.addAll(Arrays.asList(sortedPalette));
		structure.put("palette", palette);

		// Blocks
		NbtList blocks = new NbtList();
		for (SkeletonBlock block : blockData) {
			NbtCompound nbtBlock = new NbtCompound();
			NbtList posList = new NbtList();
			posList.add(NbtInt.of(block.x()));
			posList.add(NbtInt.of(block.y()));
			posList.add(NbtInt.of(block.z()));
			nbtBlock.put("pos", posList);
			nbtBlock.put("state", NbtInt.of(block.blockType() - 1)); // Subtract 1 because NUMERIC_ID starts at 1.
			blocks.add(nbtBlock);
		}
		structure.put("blocks", blocks);
		return structure;
	}

	/**
	 * Calculates the room size by finding the smallest and largest X, Y, and Z coordinates.
	 * Generally, it's always going to be one of a few possibilities depending on room size...
	 */
	static NbtList getSize(List<SkeletonBlock> blockData) {
		// Min and Max are initially set to first block and last blocks respectively
		int minX = blockData.getFirst().x();
		int minY = 0; // Intentionally set to 0 so that it can be pasted in at the correct height.
		int minZ = blockData.getFirst().z();
		int maxX = blockData.getLast().x();
		int maxY = blockData.getLast().y();
		int maxZ = blockData.getLast().z();

		for (SkeletonBlock block : blockData) {
			if (block.x() > maxX) maxX = block.x();
			if (block.x() < minX) minX = block.x();
			if (block.y() > maxY) maxY = block.y();
			if (block.y() < minY) minY = block.y();
			if (block.z() > maxZ) maxZ = block.z();
			if (block.z() < minZ) minZ = block.z();
		}

		NbtList sizeList = new NbtList();
		sizeList.add(NbtInt.of(maxX - minX));
		sizeList.add(NbtInt.of(maxY - minY));
		sizeList.add(NbtInt.of(maxZ - minZ));
		return sizeList;
	}

	// Simple data class to store all the block info.
	public record SkeletonBlock(int x, int y, int z, byte blockType) {}
}
