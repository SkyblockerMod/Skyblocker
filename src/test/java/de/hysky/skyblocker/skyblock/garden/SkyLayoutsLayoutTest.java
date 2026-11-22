package de.hysky.skyblocker.skyblock.garden;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class SkyLayoutsLayoutTest {
	// The kinds are built from GreenhouseCrops, which needs the item registries
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	// https://skylayouts.io/l/s1zmd9hGL38uvpNOY resolves to this, with the same board on all three plots
	private static final String PACKED_BOARD = "3KFfpqNeBaykSGkOcMXco3aJzxj7fPJlefxVxK5DR1HaIEQ6LmGA";
	private static final String LONG_LINK = "https://skylayouts.io/l/1AF~p" + PACKED_BOARD + "~1~1";

	// The same board, with N for Noctilume, S for Snoozling, G for Soggybud, C for Cocoa Beans and W for Nether Wart
	private static final String[] BOARD = {
			"..NNSSSNN.",
			"GGNNSSSNNC",
			"G.NNSSS.NN",
			"GGNN.SSSNN",
			"G.GNNSSSW.",
			"GNNNNSSSNN",
			"CNN.SSS.NN",
			".SSSSSSNN.",
			".SSSSSSNN.",
			".SSS......",
	};
	// The order the kinds appear in the link: Noctilume, Snoozling, Soggybud, Cocoa Beans, Nether Wart
	private static final String BOARD_KINDS = "NSGCW";
	private static final Map<Character, Integer> CROP_IDS = Map.of('N', 22, 'S', 23, 'G', 24, 'C', 41, 'W', 53);
	// SkyLayouts shows 4 Stoplight Petal spots and 2 Thunderling spots for this board
	private static final Set<String> SPOTS = Set.of("2,7", "3,4", "6,3", "6,7", "2,1", "4,1");

	// Rows run along +z and columns along +x
	private static int at(int[][] grid, int row, int col) {
		return grid[col][row];
	}

	@Test
	void decodePackedBoards() {
		assertBoard(SkyLayoutsLayout.decode(LONG_LINK));
		assertBoard(SkyLayoutsLayout.decode("/l/1AF~p" + PACKED_BOARD + "~1~1"));
		assertBoard(SkyLayoutsLayout.decode("1AF~p" + PACKED_BOARD));
		assertBoard(SkyLayoutsLayout.decode(LONG_LINK + "#s1=abc"));
		assertBoard(SkyLayoutsLayout.decode(LONG_LINK + "/"));
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF%7Ep" + PACKED_BOARD));
	}

	@Test
	void decodeFirstPlotWithCrops() {
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p~" + PACKED_BOARD));
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p3KAB~" + PACKED_BOARD));
	}

	@Test
	void decodeFragmentBoards() {
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/layout.html?m=STOPLIGHT_PETAL&i=5#b1=" + PACKED_BOARD + "&r1=abc"));
		assertBoard(SkyLayoutsLayout.decode("/layout.html?i=5#s1=abc&b2=" + PACKED_BOARD));
	}

	@Test
	void decodeDeflatedBoards() {
		byte[] cells = new byte[100];
		for (int cell = 0; cell < 100; cell++) {
			cells[cell] = (byte) (BOARD_KINDS.indexOf(BOARD[cell / 10].charAt(cell % 10)) + 1);
		}

		// Size 10, plot 1 only, then the five kinds in link order
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~zKBFfpqNe" + deflateRaw(cells)));

		// Plots 2 and 3, where plot 2 is empty
		byte[] twoPlots = Arrays.copyOf(new byte[100], 200);
		System.arraycopy(cells, 0, twoPlots, 100, 100);
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~zKGFfpqNe" + deflateRaw(twoPlots)));

		// The data must hold exactly one byte per cell of every plot in the mask
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~zKDFfpqNe" + deflateRaw(cells)));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~zKBFfpqNe" + deflateRaw(twoPlots)));
	}

	@Test
	void shortLinkId() {
		Assertions.assertEquals("s1zmd9hGL38uvpNOY", SkyLayoutsLayout.shortLinkId("https://skylayouts.io/l/s1zmd9hGL38uvpNOY"));
		Assertions.assertEquals("s1zmd9hGL38uvpNOY", SkyLayoutsLayout.shortLinkId("skylayouts.io/l/s1zmd9hGL38uvpNOY?ref=discord"));
		Assertions.assertEquals("s1zmd9hGL38uvpNOY", SkyLayoutsLayout.shortLinkId("https://skylayouts.io/l/s1zmd9hGL38uvpNOY#x"));

		Assertions.assertNull(SkyLayoutsLayout.shortLinkId(LONG_LINK));
		Assertions.assertNull(SkyLayoutsLayout.shortLinkId("https://skylayouts.io/l/s1zmd9hGL38uvpNOYZ"));
		Assertions.assertNull(SkyLayoutsLayout.shortLinkId("https://skylayouts.io/l/s1zmd9hGL38uvpNO"));
	}

	@Test
	void rejectsMalformedLinks() {
		Assertions.assertNull(SkyLayoutsLayout.decode(""));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF"));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p"));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p3KAB"));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/2AF~p" + PACKED_BOARD));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~q" + PACKED_BOARD));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/s1zmd9hGL38uvpNOY"));

		// Wrong size, unknown kinds, and a number that holds more or less than 100 cells
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p" + PACKED_BOARD.replaceFirst("K", "J")));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p" + PACKED_BOARD.replaceFirst("f", "_")));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p" + PACKED_BOARD.substring(0, 8) + "B" + PACKED_BOARD.substring(8)));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p" + PACKED_BOARD.substring(0, PACKED_BOARD.length() - 1)));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p" + PACKED_BOARD + "!"));

		// Not SkyLayouts layouts
		Assertions.assertNull(SkyLayoutsLayout.decode("M9AxzKjJrknUIx4k6ZEFHAE"));
		Assertions.assertNull(SkyLayoutsLayout.decode("https://skymutations.eu/greenhouse?layout=NrBMBoIIgLQewLYCMCuATK4AMBdcZwAWcWRVDbPAgNhPmXU13wgA"));
	}

	@Test
	void namesMatchGreenhouseCrops() {
		Assertions.assertEquals(62, SkyLayoutsLayout.KINDS.length);
		for (String name : SkyLayoutsLayout.KINDS) {
			if (name != null) Assertions.assertNotNull(GreenhouseCrops.CROP_ID_MAP.get(name), name);
		}

		for (Map<String, Integer> recipe : SkyLayoutsLayout.MUTATION_RECIPES) {
			for (String name : recipe.keySet()) {
				Assertions.assertNotNull(GreenhouseCrops.CROP_ID_MAP.get(name), name);
			}
		}
	}

	private static void assertBoard(int[][] grid) {
		Assertions.assertNotNull(grid);

		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				char kind = BOARD[row].charAt(col);
				int expected = kind != '.' ? CROP_IDS.get(kind) : SPOTS.contains(row + "," + col) ? 0 : -1;

				Assertions.assertEquals(expected, at(grid, row, col), "row " + row + " col " + col);
			}
		}
	}

	private static String deflateRaw(byte[] contents) {
		try (Deflater deflater = new Deflater(9, true)) {
			deflater.setInput(contents);
			deflater.finish();

			byte[] buffer = new byte[1024];
			int length = deflater.deflate(buffer);

			return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(buffer, length));
		}
	}
}
