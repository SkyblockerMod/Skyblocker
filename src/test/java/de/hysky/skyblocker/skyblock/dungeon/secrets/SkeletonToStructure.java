package de.hysky.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * This is the opposite of {@link StructureToSkeleton}, for more detailed comments see that class instead.
 */
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
		if (Files.exists(outputPath)) throw new RuntimeException("Output file already exists! - %s".formatted(outputPath));

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
		return Arrays.stream(blocks).mapToObj(SkeletonBlock::from).toList();
	}

	/**
	 * Manually creates a structure .nbt file from the skeleton blocks.
	 * A structure template is an NbtCompound of "size", "palette", and "blocks"
	 */
	static void writeStructureFile(List<SkeletonBlock> blockData, Path outputPath) throws IOException {
		CompoundTag structureFile = new CompoundTag();

		// Structure Size
		structureFile.put("size", getSize(blockData));

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
		structureFile.put("palette", palette);

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
		structureFile.put("blocks", blocks);

		NbtIo.writeCompressed(structureFile, outputPath);
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
