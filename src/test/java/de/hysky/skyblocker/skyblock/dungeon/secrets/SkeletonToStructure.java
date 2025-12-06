package de.hysky.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * This is the opposite of {@link StructureToSkeleton}, for more detailed comments see that class instead.
 */
@SuppressWarnings("JavadocReference")
public class SkeletonToStructure {
	private static final Logger LOGGER = LoggerFactory.getLogger("SkeletonToStructure");

	/**
	 * Argument #1: the path to the .skeleton file.
	 * If successful, the structure .nbt file will be stored next to the .skeleton file.
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (args.length != 1) throw new RuntimeException("One argument must be provided!");

		convertToStructure(args[0]);
	}

	static void convertToStructure(String inputPath) throws IOException, ClassNotFoundException {
		Path outputPath = Path.of(inputPath + ".nbt");
		if (outputPath.toFile().exists()) throw new RuntimeException("Output file already exists! - %s".formatted(outputPath));

		int[] blocks = readSkeletonFile(Path.of(inputPath));
		List<SkeletonBlock> blockData = createBlockList(blocks);
		writeStructureFile(blockData, outputPath);
		LOGGER.info("Successfully converted to .nbt - {}", outputPath);
	}

	/**
	 * Opposite of {@link StructureToSkeleton#writeSkeletonFile(Path, int[])}
	 */
	static int[] readSkeletonFile(Path path) throws IOException, ClassNotFoundException {
		try (ObjectInputStream inputStream = new ObjectInputStream(new InflaterInputStream(Files.newInputStream(path)))) {
			return (int[]) inputStream.readObject();
		}
	}

	/**
	 * Opposite of {@link StructureToSkeleton#createBlockArray(List)}
	 */
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
	static void writeStructureFile(List<SkeletonBlock> blockData, Path outputPath) throws IOException {
		NbtCompound structureFile = new NbtCompound();

		// Structure Size
		structureFile.put("size", getSize(blockData));

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
		structureFile.put("palette", palette);

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
		structureFile.put("blocks", blocks);

		NbtIo.writeCompressed(structureFile, outputPath);
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
}
