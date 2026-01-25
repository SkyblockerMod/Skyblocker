package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.skyblock.dungeon.preview.SkeletonBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;

/**
 * Utility class to convert a structure template NBT file into a .skeleton file through an easy 3-step process.
 * A .skeleton file is a sorted int[] where each integer is (X << 24) | (Y << 16) | (Z << 8) | blockType
 */
public class StructureToSkeleton {
	private static final Logger LOGGER = LoggerFactory.getLogger("StructureToSkeleton");

	/**
	 * Argument #1: the path to the structure .nbt file
	 * Argument #2: optional - the y-offset of the .structure block, 0 if not provided.
	 * If successful, the .skeleton file will be stored next to the structure .nbt file.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) throw new RuntimeException("Insufficient arguments provided!");
		if (args.length > 2) throw new RuntimeException("Too many arguments provided!");

		convertToSkeleton(args[0], args.length == 2 ? Integer.parseInt(args[1]) : 0);
	}

	/**
	 * Combines the 3 steps of the process outlined below.
	 */
	static void convertToSkeleton(String structureTemplatePath, int baseY) throws IOException {
		Path outputPath = Path.of(structureTemplatePath + ".skeleton");
		if (Files.exists(outputPath)) throw new RuntimeException("Output file already exists! - %s".formatted(outputPath));

		List<SkeletonBlock> blockData = getStructureBlocks(structureTemplatePath, baseY);
		int[] blockNums = createBlockArray(blockData);
		writeSkeletonFile(outputPath, blockNums);
		LOGGER.info("Successfully converted to .skeleton - {}", outputPath);
	}

	/**
	 * Step #1: Read the structure template file to get the block information and palette information.
	 * Then, map the palette to the .skeleton palette, not every block type is included.
	 * Finally, iterate through all the blocks and convert it into a list.
	 * <br>
	 * Manually parses through NBT because StructureTemplate is harder to use...
	 *
	 * @param yOffset The Y-height of the lowest block in the structure template.
	 *                NOTE: The lowest block can be air depending on how it is saved!!
	 */
	static List<SkeletonBlock> getStructureBlocks(String structureFilePath, int yOffset) throws IOException {
		List<SkeletonBlock> blockData = new ArrayList<>();
		CompoundTag nbtData = NbtIo.readCompressed(Path.of(structureFilePath), NbtAccounter.unlimitedHeap());
		if (!nbtData.contains("palette") || !nbtData.contains("blocks")) throw new RuntimeException("Invalid structure NBT");

		ListTag nbtPalette = Objects.requireNonNull(nbtData.get("palette")).asList().orElseThrow();
		List<Byte> palette = nbtPalette.stream()
				.map(nbtElem -> Objects.requireNonNull(nbtElem.asCompound().orElseThrow().get("Name")).asString().orElseThrow())
				.map(blockName -> DungeonManager.NUMERIC_ID.getOrDefault(blockName, (byte) 0))
				.toList();

		ListTag nbtBlocks = Objects.requireNonNull(nbtData.get("blocks")).asList().orElseThrow();
		nbtBlocks.forEach(nbtElem -> {
			CompoundTag block = nbtElem.asCompound().orElseThrow();
			byte blockType = palette.get(Objects.requireNonNull(block.get("state")).asInt().orElseThrow());
			if (blockType == 0) return; // Invalid block.

			ListTag blockPos = Objects.requireNonNull(block.get("pos")).asList().orElseThrow();
			blockData.add(new SkeletonBlock(
					blockPos.getInt(0).orElseThrow(), blockPos.getInt(1).orElseThrow() + yOffset, blockPos.getInt(2).orElseThrow(), blockType
			));
		});

		return blockData;
	}

	/**
	 * Step #2: For each block: calculate the "block number".
	 * The block number is the following 4 bytes: blockX, blockY, blockZ, blockType
	 * We then sort and append all into an array.
	 */
	static int[] createBlockArray(List<SkeletonBlock> blockData) {
		int initialSize = blockData.size();
		List<Integer> blockNums = blockData.stream()
				.map(SkeletonBlock::compress)
				.filter((num) -> num > 0)
				.sorted().toList();
		if (blockNums.size() != initialSize) throw new RuntimeException("Negative block number detected..");

		int[] blocks = new int[blockData.size()];
		for (int i = 0; i < blockNums.size(); i++) {
			blocks[i] = blockNums.get(i);
		}
		return blocks;
	}

	/**
	 * Step #3: Write to a file using ObjectOutputStream.
	 */
	static void writeSkeletonFile(Path path, int[] blocks) throws IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new DeflaterOutputStream(Files.newOutputStream(path)))) {
			outputStream.writeObject(blocks);
		}
	}
}
