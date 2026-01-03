package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.skyblock.dungeon.preview.RoomStructure;
import de.hysky.skyblocker.skyblock.dungeon.preview.SkeletonBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
		List<SkeletonBlock> blockData = RoomStructure.createBlockList(blocks);
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
	 * Creates the NBT Compound and saves it to a file from the skeleton blocks.
	 * See {@link RoomStructure#createCompound(List)}
	 */
	static void writeStructureFile(List<SkeletonBlock> blockData, Path outputPath) throws IOException {
		CompoundTag structureFile = RoomStructure.createCompound(blockData);
		NbtIo.writeCompressed(structureFile, outputPath);
	}
}
