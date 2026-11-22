package de.hysky.skyblocker.skyblock.garden;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

	private static final String DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

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
	// Wheat (kind 52, digit 0) in the top left cell only
	private static final String WHEAT_BOARD = pack(wheatInTopLeft(), "0");

	private static int[] wheatInTopLeft() {
		int[] cells = new int[100];
		cells[0] = 1;
		return cells;
	}

	// Rows run along +z and columns along +x
	private static int at(int[][] grid, int row, int col) {
		return grid[col][row];
	}

	@Test
	void packMatchesTheSite() {
		int[] cells = new int[100];
		for (int cell = 0; cell < 100; cell++) {
			cells[cell] = BOARD_KINDS.indexOf(BOARD[cell / 10].charAt(cell % 10)) + 1;
		}
		Assertions.assertEquals(PACKED_BOARD, pack(cells, "fpqNe"));
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
	void decodeViewedPlot() {
		// A 2 or 3 flag in the header, or the p parameter, is the plot the site was showing
		Assertions.assertEquals(50, at(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF2~p" + PACKED_BOARD + "~" + WHEAT_BOARD), 0, 0));
		Assertions.assertEquals(50, at(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AFs3~p" + PACKED_BOARD + "~1~" + WHEAT_BOARD), 0, 0));
		Assertions.assertEquals(50, at(SkyLayoutsLayout.decode("https://skylayouts.io/layout.html?p=2#b1=" + PACKED_BOARD + "&b2=" + WHEAT_BOARD), 0, 0));
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF~p" + PACKED_BOARD + "~" + WHEAT_BOARD));

		// A viewed plot without crops falls back to the first plot with crops
		assertBoard(SkyLayoutsLayout.decode("https://skylayouts.io/l/1AF3~p" + PACKED_BOARD + "~1~3KAB"));
	}

	@Test
	void inferBigAndIsolatedSpots() {
		// Duskbloom (kind 18, digit S) on the top and left of a ring around rows 1-2 and columns 1-2, Lonelily (kind 26, digit a) on the rest
		int[] cells = new int[100];
		int[][] ring = {{0, 0}, {0, 1}, {0, 2}, {0, 3}, {1, 0}, {1, 3}, {2, 0}, {2, 3}, {3, 0}, {3, 1}, {3, 2}, {3, 3}};
		for (int i = 0; i < ring.length; i++) {
			cells[ring[i][0] * 10 + ring[i][1]] = i < 6 ? 1 : 2;
		}
		int[][] grid = SkyLayoutsLayout.decode("/layout.html#b1=" + pack(cells, "Sa"));
		Assertions.assertNotNull(grid);

		// 6 Duskbloom and 6 Lonelily around an empty 2x2 spawn a Noctilume there
		for (int row = 1; row <= 2; row++) {
			for (int col = 1; col <= 2; col++) {
				Assertions.assertEquals(0, at(grid, row, col), "row " + row + " col " + col);
			}
		}
		// Lonelily spawns where no crop is around, and nothing spawns next to just 2 Duskbloom
		Assertions.assertEquals(0, at(grid, 5, 5));
		Assertions.assertEquals(0, at(grid, 9, 9));
		Assertions.assertEquals(-1, at(grid, 0, 4));

		// With a crop in the 2x2 it can't spawn
		cells[11] = 1;
		grid = SkyLayoutsLayout.decode("/layout.html#b1=" + pack(cells, "Sa"));
		Assertions.assertNotNull(grid);
		Assertions.assertEquals(14, at(grid, 1, 1));
		Assertions.assertEquals(-1, at(grid, 2, 2));
	}

	@Test
	void decodeOlderFragmentBoards() {
		// Run length boards: Wheat in cell 0, 9 empty cells, then Noctilume in cell 10
		int[][] grid = SkyLayoutsLayout.decode("https://skylayouts.io/layout.html#b1=2~a~WHEAT,NOCTILUME~0.91");
		Assertions.assertNotNull(grid);
		Assertions.assertEquals(50, at(grid, 0, 0));
		Assertions.assertEquals(22, at(grid, 1, 0));
		Assertions.assertNotNull(SkyLayoutsLayout.decode("/layout.html#b1=2~~WHEAT~0"));
		Assertions.assertNull(SkyLayoutsLayout.decode("/layout.html#b1=2~9~WHEAT~0"));

		// Boards listing the column and row of each kind's cells, where ground blocks aren't crops
		grid = SkyLayoutsLayout.decode("https://skylayouts.io/layout.html#b1=WHEAT=0011|SAND=22|NOCTILUME=35");
		Assertions.assertNotNull(grid);
		Assertions.assertEquals(50, at(grid, 0, 0));
		Assertions.assertEquals(50, at(grid, 1, 1));
		Assertions.assertEquals(22, at(grid, 5, 3));
		Assertions.assertNotEquals(50, at(grid, 2, 2));

		// b is the board of the plot that was showing, unless that plot has its own
		Assertions.assertEquals(50, at(SkyLayoutsLayout.decode("/layout.html?p=2#b1=" + PACKED_BOARD + "&b=" + WHEAT_BOARD), 0, 0));
		assertBoard(SkyLayoutsLayout.decode("/layout.html?p=2#b=" + WHEAT_BOARD + "&b2=" + PACKED_BOARD));
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
	void bundledBoardKeys() {
		// Ranked boards name a board for each plot
		Assertions.assertEquals(List.of(List.of("dustgrain_s5_plot1"), List.of("dustgrain_s5_plot2"), List.of("dustgrain_s5_plot3")),
				SkyLayoutsLayout.bundledBoardKeys("https://skylayouts.io/layout.html?m=DUSTGRAIN&i=5&ranked=dustgrain_s5_plot1%2Cdustgrain_s5_plot2%2Cdustgrain_s5_plot3"));
		Assertions.assertEquals(List.of(List.of(), List.of("dustgrain_s5_c_p2_plot2"), List.of()),
				SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=DUSTGRAIN&i=5&ranked=,dustgrain_s5_c_p2_plot2"));
		Assertions.assertEquals(List.of(List.of("chorus_fruit_s14_plot1"), List.of("chorus_fruit_s14_plot2"), List.of("chorus_fruit_s14_plot3")),
				SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=CHORUS_FRUIT&i=5&ri=14&ranked=chorus_fruit_s14_plot1,chorus_fruit_s14_plot2,chorus_fruit_s14_plot3"));

		// Without ranked boards, or with ones the site would ignore, it shows its own board for the mutation and interval
		List<List<String>> dustgrain = List.of(
				List.of("dustgrain_s5_plot1", "dustgrain_s5"), List.of("dustgrain_s5_plot2"), List.of("dustgrain_s5_plot3"));
		Assertions.assertEquals(dustgrain, SkyLayoutsLayout.bundledBoardKeys("https://skylayouts.io/layout.html?m=DUSTGRAIN&i=5"));
		Assertions.assertEquals(dustgrain, SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=DUSTGRAIN&i=5&ranked=wheat_s5_plot1"));
		Assertions.assertEquals(dustgrain, SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=DUSTGRAIN&i=5&ranked=dustgrain_s5_plot2"));
		Assertions.assertEquals(List.of("glasscorn_s1_c_plot1__noswap", "glasscorn_s1_plot1__noswap", "glasscorn_s1_c_plot1", "glasscorn_s1_plot1", "glasscorn_s1"),
				SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=GLASSCORN&i=1&l=collection&v=noswap").getFirst());
		Assertions.assertEquals("blastberry_s14_plot1", SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=BLASTBERRY_PLAIN&i=14").getFirst().getFirst());

		// Links that hold their own boards, or show none
		Assertions.assertEquals(List.of(), SkyLayoutsLayout.bundledBoardKeys(LONG_LINK));
		Assertions.assertEquals(List.of(), SkyLayoutsLayout.bundledBoardKeys("/layout.html#b1=" + PACKED_BOARD));
		Assertions.assertEquals(List.of(), SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=blank&i=5"));
		Assertions.assertEquals(List.of(), SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=DUSTGRAIN"));
		Assertions.assertEquals(List.of(), SkyLayoutsLayout.bundledBoardKeys("/layout.html?m=DUSTGRAIN&i=-5"));
	}

	@Test
	void dataFileOf() {
		Assertions.assertEquals("boards.json", SkyLayoutsLayout.dataFileOf("dustgrain_s5_plot1"));
		Assertions.assertEquals("boards.json", SkyLayoutsLayout.dataFileOf("glasscorn_s1_plot1__noswap"));
		Assertions.assertEquals("boards-plots.json", SkyLayoutsLayout.dataFileOf("dustgrain_s5_p2_plot1"));
		Assertions.assertEquals("boards-collection.json", SkyLayoutsLayout.dataFileOf("dustgrain_s5_c_plot1"));
		Assertions.assertEquals("boards-collection.json", SkyLayoutsLayout.dataFileOf("devourer_s1_c_p1_plot1"));
		Assertions.assertEquals("boards-devourer.json", SkyLayoutsLayout.dataFileOf("devourer_s1_p1_plot1"));
		Assertions.assertEquals("boards-devourer.json", SkyLayoutsLayout.dataFileOf("devourer_s1_plot1"));
	}

	@Test
	void decodeBundledBoard() {
		JsonObject json = JsonParser.parseString("""
				{
					"size": 10,
					"tiles": [
						{"x": 1, "y": 2, "kind": "WHEAT"}, {"x": 1, "y": 3, "kind": "WHEAT"},
						{"x": 3, "y": 3, "kind": "NOCTILUME"}, {"x": 4, "y": 3, "kind": "NOCTILUME", "ax": 3, "ay": 3},
						{"x": 3, "y": 4, "kind": "NOCTILUME", "ax": 3, "ay": 3}, {"x": 4, "y": 4, "kind": "NOCTILUME", "ax": 3, "ay": 3},
						{"x": 0, "y": 0, "kind": "FARMLAND"}, {"x": 9, "y": 9, "kind": "MUSHROOM"}, {"x": 8, "y": 9, "kind": "SOME_NEW_CROP"}
					],
					"footprint": [2, 2],
					"spots": [{"x": 6, "y": 6}, {"x": 9, "y": 0}]
				}
				""").getAsJsonObject();
		int[][] grid = SkyLayoutsLayout.decodeBundledBoard(json);
		Assertions.assertNotNull(grid);

		Map<String, Integer> expected = Map.ofEntries(
				Map.entry("2,1", 50), Map.entry("3,1", 50),
				Map.entry("3,3", 22), Map.entry("3,4", 22), Map.entry("4,3", 22), Map.entry("4,4", 22),
				Map.entry("9,9", 49),
				// Spots cover the footprint from their top left corner, cut off at the edge
				Map.entry("6,6", 0), Map.entry("6,7", 0), Map.entry("7,6", 0), Map.entry("7,7", 0),
				Map.entry("0,9", 0), Map.entry("1,9", 0));
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				// Only the listed spots count, so the cells next to both wheat aren't Dustgrain spots here
				Assertions.assertEquals(expected.getOrDefault(row + "," + col, -1), at(grid, row, col), "row " + row + " col " + col);
			}
		}

		Assertions.assertNull(SkyLayoutsLayout.decodeBundledBoard(JsonParser.parseString("{\"size\": 9, \"tiles\": []}").getAsJsonObject()));
		Assertions.assertNull(SkyLayoutsLayout.decodeBundledBoard(JsonParser.parseString("{\"size\": 10}").getAsJsonObject()));
	}

	@Test
	void namesMatchGreenhouseCrops() {
		Assertions.assertEquals(62, SkyLayoutsLayout.KINDS.size());
		for (SkyLayoutsLayout.Kind kind : SkyLayoutsLayout.KINDS) {
			if (kind.crop() != null) Assertions.assertNotNull(GreenhouseCrops.CROP_ID_MAP.get(kind.crop()), kind.crop());
		}

		for (SkyLayoutsLayout.Recipe recipe : SkyLayoutsLayout.MUTATION_RECIPES) {
			for (String name : recipe.neighbours().keySet()) {
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

	/**
	 * Packs a board the way SkyLayouts does.
	 *
	 * @param cells 0 for empty, or the index into {@code kinds} plus one, row major
	 * @param kinds the kinds as digits
	 */
	private static String pack(int[] cells, String kinds) {
		BigInteger base = BigInteger.valueOf(kinds.length() + 1);
		BigInteger number = BigInteger.ONE;
		for (int cell : cells) {
			number = number.multiply(base).add(BigInteger.valueOf(cell));
		}

		StringBuilder digits = new StringBuilder();
		for (; number.signum() > 0; number = number.shiftRight(6)) {
			digits.insert(0, DIGITS.charAt(number.intValue() & 63));
		}
		return "3K" + DIGITS.charAt(kinds.length()) + kinds + digits;
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
