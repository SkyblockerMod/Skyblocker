package de.hysky.skyblocker.skyblock.dungeon.preview;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.Arrays;
import java.util.List;

public class RoomStructure {
	static CompoundTag getCompound(int[] blocks) {
		return createCompound(createBlockList(blocks));
	}

	public static List<SkeletonBlock> createBlockList(int[] blocks) {
		return Arrays.stream(blocks).mapToObj(SkeletonBlock::from).toList();
	}

	/**
	 * Manually creates a structure NBT Compound from the skeleton blocks.
	 * A structure template is an CompoundTag of "size", "palette", and "blocks"
	 */
	public static CompoundTag createCompound(List<SkeletonBlock> blockData) {
		CompoundTag structure = new CompoundTag();

		// Structure Size
		structure.put("size", getSize(blockData));

		// Palette
		CompoundTag[] sortedPalette = new CompoundTag[DungeonManager.NUMERIC_ID.size()];
		// The entries are not guaranteed to be ordered.
		for (var entry : DungeonManager.NUMERIC_ID.object2ByteEntrySet()) {
			CompoundTag paletteBlock = new CompoundTag();
			paletteBlock.put("Name", StringTag.valueOf(entry.getKey()));
			sortedPalette[entry.getByteValue() - 1] = paletteBlock; // Subtract 1 because NUMERIC_ID starts at 1.
		}
		ListTag palette = new ListTag();
		palette.addAll(Arrays.asList(sortedPalette));
		structure.put("palette", palette);

		// Blocks
		ListTag blocks = new ListTag();
		for (SkeletonBlock block : blockData) {
			CompoundTag nbtBlock = new CompoundTag();
			ListTag posList = new ListTag();
			posList.add(IntTag.valueOf(block.x()));
			posList.add(IntTag.valueOf(block.y()));
			posList.add(IntTag.valueOf(block.z()));
			nbtBlock.put("pos", posList);
			nbtBlock.put("state", IntTag.valueOf(block.blockType() - 1)); // Subtract 1 because NUMERIC_ID starts at 1.
			blocks.add(nbtBlock);
		}
		structure.put("blocks", blocks);
		return structure;
	}

	/**
	 * Calculates the room size by finding the smallest and largest X, Y, and Z coordinates.
	 * Generally, it's always going to be one of a few possibilities depending on room size...
	 */
	static ListTag getSize(List<SkeletonBlock> blockData) {
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

		ListTag sizeList = new ListTag();
		sizeList.add(IntTag.valueOf(maxX - minX));
		sizeList.add(IntTag.valueOf(maxY - minY));
		sizeList.add(IntTag.valueOf(maxZ - minZ));
		return sizeList;
	}
}
