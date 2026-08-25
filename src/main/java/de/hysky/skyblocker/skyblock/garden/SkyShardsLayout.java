package de.hysky.skyblocker.skyblock.garden;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.jspecify.annotations.Nullable;

/**
 * Decodes greenhouse layouts from <a href="https://greenhouse.skyshards.com">SkyShards Greenhouse</a>.
 * The code is a raw deflated, base64url encoded string of the form {@code inputs|targets|grid}.
 * Both palettes are comma separated base 36 indices into {@link #INDEX_TO_CROP_ID}, and the grid is a
 * 10x10 row major grid of one or two character cells, lowercase for an input, uppercase for a target and
 * {@code .} for empty. A cell only marks the top left corner of a placement.
 */
public final class SkyShardsLayout {
	private static final int GRID_SIZE = 10;
	private static final int TOTAL_CELLS = GRID_SIZE * GRID_SIZE;
	private static final int LETTERS = 26;
	private static final int MAX_DECOMPRESSED_BYTES = 4096;

	// Palette index to crop id. Mutations happen to line up with crop ids 1-40.
	private static final int[] INDEX_TO_CROP_ID = {
			// Crops
			50, // wheat
			52, // potato
			51, // carrot
			47, // pumpkin
			46, // melon
			41, // cocoa_beans
			54, // sugar_cane
			45, // cactus
			53, // nether_wart
			49, // red_mushroom
			48, // brown_mushroom
			43, // moonflower
			44, // sunflower
			42, // wild_rose
			56, // fire
			55, // dead_plant
			58, // fermento

			// Mutations
			1,  // ashwreath
			2,  // choconut
			3,  // dustgrain
			4,  // gloomgourd
			5,  // lonelily
			6,  // scourroot
			7,  // shadevine
			8,  // veilshroom
			9,  // witherbloom
			10, // chocoberry
			11, // cindershade
			12, // coalroot
			13, // creambloom
			14, // duskbloom
			15, // thornshade
			16, // blastberry
			17, // cheesebite
			18, // chloronite
			19, // do_not_eat_shroom
			20, // fleshtrap
			21, // magic_jellybean
			22, // noctilume
			23, // snoozling
			24, // soggybud
			25, // chorus_fruit
			26, // plantboy_advance
			27, // puffercloud
			28, // shellfruit
			29, // startlevine
			30, // stoplight_petal
			31, // thunderling
			32, // turtlellini
			33, // zombud
			34, // all_in_aloe
			35, // devourer
			36, // glasscorn
			37, // godseed
			38, // jerryflower
			39, // phantomleaf
			40, // timestalk
	};

	private SkyShardsLayout() {
	}

	/**
	 * Pulls the layout code out of a designer link, a share link, or a bare code.
	 */
	public static String extractLayoutCode(String clipboard) {
		String trimmed = clipboard.strip();

		int layoutIndex = trimmed.indexOf("?layout=");
		if (layoutIndex < 0) layoutIndex = trimmed.indexOf("&layout=");
		if (layoutIndex >= 0) return endOfCode(trimmed.substring(layoutIndex + "?layout=".length()));

		int shareIndex = trimmed.indexOf("/share/");
		if (shareIndex >= 0) return endOfCode(trimmed.substring(shareIndex + "/share/".length()));

		return trimmed;
	}

	// Cuts off trailing query parameters and path segments
	private static String endOfCode(String code) {
		for (int i = 0; i < code.length(); i++) {
			char c = code.charAt(i);
			boolean valid = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '-' || c == '_' || c == '=';
			if (!valid) return code.substring(0, i);
		}
		return code;
	}

	/**
	 * Decodes the layout into a target greenhouse grid, indexed {@code [x][z]} from the north west corner.
	 * Cells are -1 where the layout says nothing, 0 for a desired mutation, or a crop id.
	 *
	 * @return the grid, or null if the code isn't a SkyShards layout
	 */
	public static int @Nullable [][] decode(String code) {
		try {
			String gridString = decompress(code);
			if (gridString == null) return null;

			String[] parts = gridString.split("\\|", -1);
			if (parts.length != 3) return null;

			int[] inputPalette = parsePalette(parts[0]);
			int[] targetPalette = parsePalette(parts[1]);
			if (inputPalette == null || targetPalette == null) return null;

			String grid = parts[2];
			int cellWidth = grid.length() == TOTAL_CELLS ? 1 : grid.length() == TOTAL_CELLS * 2 ? 2 : 0;
			if (cellWidth == 0) return null;

			// Decoded in screen space first, so placements expand from their top left corner
			int[][] screen = new int[GRID_SIZE][GRID_SIZE];
			for (int[] row : screen) Arrays.fill(row, -1);

			for (int cell = 0; cell < TOTAL_CELLS; cell++) {
				String chars = grid.substring(cell * cellWidth, (cell + 1) * cellWidth);
				int paletteIndex = paletteIndexOf(chars);
				if (paletteIndex < 0) continue;

				boolean target = chars.equals(chars.toUpperCase());
				int[] palette = target ? targetPalette : inputPalette;
				if (paletteIndex >= palette.length) continue;

				int cropId = palette[paletteIndex];
				int value = target ? 0 : cropId; // Desired mutation spot should be empty

				int row = cell / GRID_SIZE;
				int col = cell % GRID_SIZE;
				int size = sizeOf(cropId);
				for (int dr = 0; dr < size && row + dr < GRID_SIZE; dr++) {
					for (int dc = 0; dc < size && col + dc < GRID_SIZE; dc++) {
						screen[row + dr][col + dc] = value;
					}
				}
			}

			// The top of the layout faces north, so rows run along +z and columns along +x
			int[][] target = new int[GRID_SIZE][GRID_SIZE];
			for (int row = 0; row < GRID_SIZE; row++) {
				for (int col = 0; col < GRID_SIZE; col++) {
					target[col][row] = screen[row][col];
				}
			}
			return target;
		} catch (Exception _) {
			return null;
		}
	}

	private static @Nullable String decompress(String code) throws Exception {
		byte[] compressed = Base64.getUrlDecoder().decode(code);

		// Raw deflate, no zlib header
		try (InputStream in = new InflaterInputStream(new ByteArrayInputStream(compressed), new Inflater(true))) {
			byte[] decompressed = in.readNBytes(MAX_DECOMPRESSED_BYTES);
			if (in.read() != -1) return null;
			return new String(decompressed, StandardCharsets.UTF_8);
		}
	}

	private static int @Nullable [] parsePalette(String palette) {
		if (palette.isEmpty()) return new int[0];

		String[] indices = palette.split(",", -1);
		int[] cropIds = new int[indices.length];
		for (int i = 0; i < indices.length; i++) {
			int index;
			try {
				index = Integer.parseInt(indices[i], 36);
			} catch (NumberFormatException _) {
				return null;
			}
			if (index < 0 || index >= INDEX_TO_CROP_ID.length) return null;
			cropIds[i] = INDEX_TO_CROP_ID[index];
		}
		return cropIds;
	}

	// a is 0 for single character cells, aa is 0 for double character cells
	private static int paletteIndexOf(String chars) {
		int index = 0;
		for (int i = 0; i < chars.length(); i++) {
			char c = Character.toLowerCase(chars.charAt(i));
			if (c < 'a' || c > 'z') return -1;
			index = index * LETTERS + (c - 'a');
		}
		// Mixed case is neither an input nor a target
		if (!chars.equals(chars.toUpperCase()) && !chars.equals(chars.toLowerCase())) return -1;
		return index;
	}

	private static int sizeOf(int cropId) {
		return switch (cropId) {
			case 23, 37 -> 3; // Snoozling, Godseed
			case 22, 26, 36 -> 2; // Noctilume, PlantBoy Advance, Glasscorn
			default -> 1;
		};
	}
}
