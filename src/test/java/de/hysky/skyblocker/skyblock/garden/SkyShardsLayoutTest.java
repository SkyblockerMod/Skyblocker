package de.hysky.skyblocker.skyblock.garden;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.utils.LZString;

public class SkyShardsLayoutTest {
	// Wheat at row 0 col 0, Godseed at row 4 col 4 and a Gloomgourd target at row 9 col 9
	private static final String SINGLE_MODE_CODE = "M9AxzKjJrknUIx4k6ZEFHAE";

	// All 17 crops then the first 10 mutations from row 0 col 0, plus a Gloomgourd target at row 9 col 9.
	// 27 input crops pushes the grid into two character cells.
	private static final String DOUBLE_MODE_CODE = "zcHXEYJAAEDBht44JgyflPKOzBFNqEPxtuHulh17DhxJOHHmwhUJZOQUlFTUNLREOnoGRibmNa5qMDO3sLSytrE12tk7ODo5e_Puw6cvF99-_AY3fyZNfw";

	// Rows run along +z and columns along +x
	private static int at(int[][] grid, int row, int col) {
		return grid[col][row];
	}

	@Test
	void decodeSingleMode() {
		int[][] grid = SkyShardsLayout.decode(SINGLE_MODE_CODE);
		Assertions.assertNotNull(grid);

		Assertions.assertEquals(50, at(grid, 0, 0));
		Assertions.assertEquals(0, at(grid, 9, 9));

		// Godseed is 3x3 and expands from its top left corner
		for (int row = 4; row <= 6; row++) {
			for (int col = 4; col <= 6; col++) {
				Assertions.assertEquals(37, at(grid, row, col));
			}
		}

		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 10; col++) {
				if (row == 0 && col == 0 || row == 9 && col == 9) continue;
				if (row >= 4 && row <= 6 && col >= 4 && col <= 6) continue;
				Assertions.assertEquals(-1, at(grid, row, col));
			}
		}
	}

	@Test
	void decodeDoubleMode() {
		int[][] grid = SkyShardsLayout.decode(DOUBLE_MODE_CODE);
		Assertions.assertNotNull(grid);

		int[] cropIds = {50, 52, 51, 47, 46, 41, 54, 45, 53, 49, 48, 43, 44, 42, 56, 55, 58};
		for (int i = 0; i < cropIds.length; i++) {
			Assertions.assertEquals(cropIds[i], at(grid, i / 10, i % 10));
		}
		for (int i = 17; i < 27; i++) {
			Assertions.assertEquals(i - 16, at(grid, i / 10, i % 10));
		}

		Assertions.assertEquals(0, at(grid, 9, 9));
		Assertions.assertEquals(-1, at(grid, 5, 5));
	}

	@Test
	void rejectsSkyMutationsLayouts() {
		Assertions.assertNull(SkyShardsLayout.decode(LZString.compressToEncodedURIComponent("[[0,0,\"Chocoberry\",1],[1,1,\"Gloomgourd\",0]]")));
	}

	@Test
	void rejectsMalformedCodes() {
		Assertions.assertNull(SkyShardsLayout.decode(""));
		Assertions.assertNull(SkyShardsLayout.decode("not base64!!"));
		Assertions.assertNull(SkyShardsLayout.decode("AAAAAAAAAAAA"));
		Assertions.assertNull(SkyShardsLayout.decode(SINGLE_MODE_CODE.substring(4)));

		Assertions.assertNull(SkyShardsLayout.decode(deflateRaw("0,1h|k|too short")));
		Assertions.assertNull(SkyShardsLayout.decode(deflateRaw("0,1h|" + ".".repeat(100))));
		Assertions.assertNull(SkyShardsLayout.decode(deflateRaw("zz|k|" + ".".repeat(100))));
	}

	@Test
	void extractLayoutCode() {
		Assertions.assertEquals("ABC", SkyShardsLayout.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=ABC"));
		Assertions.assertEquals("ABC", SkyShardsLayout.extractLayoutCode("https://greenhouse.skyshards.com/?layout=ABC"));
		Assertions.assertEquals("ABC", SkyShardsLayout.extractLayoutCode("https://api.skyshards.com/share/ABC"));
		Assertions.assertEquals("ABC", SkyShardsLayout.extractLayoutCode("  ABC  "));
		Assertions.assertEquals("ABC", SkyShardsLayout.extractLayoutCode("https://greenhouse.skyshards.com/designer?tab=1&layout=ABC&mode=x"));
		Assertions.assertEquals("ABC", SkyShardsLayout.extractLayoutCode("https://api.skyshards.com/share/ABC?foo=bar"));
		Assertions.assertEquals("a-b_c=", SkyShardsLayout.extractLayoutCode("https://greenhouse.skyshards.com/designer?layout=a-b_c="));
	}

	private static String deflateRaw(String contents) {
		Deflater deflater = new Deflater(9, true);
		deflater.setInput(contents.getBytes(StandardCharsets.UTF_8));
		deflater.finish();

		byte[] buffer = new byte[1024];
		int length = deflater.deflate(buffer);
		deflater.end();

		return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(buffer, length));
	}
}
