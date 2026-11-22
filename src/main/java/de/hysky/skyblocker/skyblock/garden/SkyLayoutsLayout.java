package de.hysky.skyblocker.skyblock.garden;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.google.gson.JsonParser;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Http;

/**
 * Decodes greenhouse layouts from <a href="https://skylayouts.io">SkyLayouts</a>.
 *
 * <p>A short link ({@code /l/s} and 16 characters) only holds an id, which {@link #resolveShortLink(String)} turns into a
 * long link through the site's API. A long link is {@code /l/1<header>~<boards>}, where the header holds site settings and
 * the boards are either
 * <ul>
 *     <li>{@code p<plot 1>~<plot 2>~<plot 3>}, each plot empty, a digit copying an earlier plot, or a packed board, or</li>
 *     <li>{@code z<size><plot mask><kind count><kinds><data>}, where the data is the base64url encoded, raw deflated cells
 *     of every plot in the mask, one byte per cell holding 0 for empty or the index into the kinds plus one.</li>
 * </ul>
 * Links the site couldn't shorten put packed boards in the fragment instead, as in {@code /layout.html?...#b1=<board>}.
 *
 * <p>A packed board is {@code 3<size><kind count><kinds><number>}, where the base 64 number is a 1 followed by one digit
 * in base {@code kind count + 1} per cell, 0 for empty. Single characters above are base64url digits, kinds index
 * {@link #KINDS}, and cells run row major from the top left. Crops bigger than one cell fill every cell they cover.
 *
 * <p>Links don't say where mutations spawn, so {@link #MUTATION_RECIPES} marks the empty cells a mutation would spawn on.
 */
public final class SkyLayoutsLayout {
	private static final String SHARE_LINK_API = "https://skylayouts.io/api/share-links/";
	private static final Pattern SHORT_LINK = Pattern.compile("/l/(s[A-Za-z0-9_-]{16})(?![A-Za-z0-9_~-])");
	private static final String DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
	private static final int GRID_SIZE = 10;
	private static final int TOTAL_CELLS = GRID_SIZE * GRID_SIZE;
	private static final int PLOTS = 3;
	private static final int MAX_CODE_LENGTH = 4096;

	/**
	 * SkyLayouts' kinds in its own order, by their {@link GreenhouseCrops} name. The ground blocks at the end
	 * (farmland, sand, soul sand, end stone, dirt and mycelium) are null since they aren't crops.
	 */
	static final @Nullable String[] KINDS = {
			"All In Aloe", "Ashwreath", "Blastberry", "Brown Mushroom", "Cactus", "Carrot", "Cheesebite", "Chloronite",
			"Chocoberry", "Choconut", "Chorus Fruit", "Cindershade", "Coalroot", "Cocoa Beans", "Creambloom", "Dead Plants",
			"Devourer", "Do Not Eat Shroom", "Duskbloom", "Dustgrain", "Fermento", "Fire", "Fleshtrap", "Glasscorn",
			"Gloomgourd", "Godseed", "Lonelily", "Magic Jellybean", "Melon Seeds", "Moonflower", "Nether Wart", "Noctilume",
			"Phantomleaf", "PlantBoy Advance", "Potato", "Puffercloud", "Pumpkin Seeds", "Red Mushroom", "Scourroot", "Shadevine",
			"Shellfruit", "Snoozling", "Soggybud", "Startlevine", "Stoplight Petal", "Sugar Cane", "Sunflower", "Thornshade",
			"Thunderling", "Timestalk", "Turtlellini", "Veilshroom", "Wheat Seeds", "Wild Rose", "Witherbloom", "Zombud",
			null, null, null, null, null, null,
	};

	/**
	 * The neighbours a single cell mutation needs around an empty cell to spawn there, counting every cell a crop covers.
	 * Lonelily, Godseed, Shellfruit and the mutations bigger than one cell spawn by other rules, so their spots aren't marked.
	 */
	static final List<Map<String, Integer>> MUTATION_RECIPES = List.of(
			Map.of("Magic Jellybean", 6, "PlantBoy Advance", 2), // All In Aloe
			Map.of("Fire", 2, "Nether Wart", 2), // Ashwreath
			Map.of("Ashwreath", 3, "Chocoberry", 5), // Blastberry
			Map.of("Creambloom", 4, "Fermento", 4), // Cheesebite
			Map.of("Coalroot", 6, "Thornshade", 2), // Chloronite
			Map.of("Choconut", 6, "Gloomgourd", 2), // Chocoberry
			Map.of("Cocoa Beans", 2), // Choconut
			Map.of("Chloronite", 5, "Magic Jellybean", 3), // Chorus Fruit
			Map.of("Ashwreath", 4, "Witherbloom", 4), // Cindershade
			Map.of("Ashwreath", 5, "Scourroot", 3), // Coalroot
			Map.of("Choconut", 8), // Creambloom
			Map.of("Puffercloud", 4, "Zombud", 4), // Devourer
			Map.of("Scourroot", 4, "Veilshroom", 4), // Do Not Eat Shroom
			Map.of("Dustgrain", 2, "Moonflower", 2, "Shadevine", 2, "Sunflower", 2), // Duskbloom
			Map.of("Wheat Seeds", 2), // Dustgrain
			Map.of("Cindershade", 4, "Lonelily", 4), // Fleshtrap
			Map.of("Melon Seeds", 1, "Pumpkin Seeds", 1), // Gloomgourd
			Map.of("Duskbloom", 3, "Sugar Cane", 5), // Magic Jellybean
			Map.of("Chorus Fruit", 4, "Shellfruit", 4), // Phantomleaf
			Map.of("Do Not Eat Shroom", 6, "Snoozling", 2), // Puffercloud
			Map.of("Carrot", 1, "Potato", 1), // Scourroot
			Map.of("Cactus", 1, "Sugar Cane", 1), // Shadevine
			Map.of("Gloomgourd", 2, "Melon Seeds", 2), // Soggybud
			Map.of("Blastberry", 4, "Cheesebite", 4), // Startlevine
			Map.of("Noctilume", 4, "Snoozling", 4), // Stoplight Petal
			Map.of("Veilshroom", 4, "Wild Rose", 4), // Thornshade
			Map.of("Noctilume", 3, "Soggybud", 5), // Thunderling
			Map.of("Chorus Fruit", 2, "Shellfruit", 2, "Stoplight Petal", 4), // Timestalk
			Map.of("Choconut", 4, "Soggybud", 4), // Turtlellini
			Map.of("Brown Mushroom", 1, "Red Mushroom", 1), // Veilshroom
			Map.of("Dead Plants", 4), // Witherbloom
			Map.of("Cindershade", 2, "Dead Plants", 4, "Fleshtrap", 2) // Zombud
	);

	private SkyLayoutsLayout() {
	}

	/**
	 * @return the id of a short link, or null if the link isn't one
	 */
	public static @Nullable String shortLinkId(String link) {
		Matcher matcher = SHORT_LINK.matcher(link);
		return matcher.find() ? matcher.group(1) : null;
	}

	/**
	 * Asks SkyLayouts for the long link behind a short link.
	 *
	 * @return the long link's path, which {@link #decode(String)} accepts
	 */
	public static CompletableFuture<String> resolveShortLink(String id) {
		return CompletableFuture.supplyAsync(() -> {
			try (Http.ApiResponse response = Http.sendCacheableGetRequest(SHARE_LINK_API + id, null)) {
				if (!response.ok()) throw new IOException("SkyLayouts returned status " + response.statusCode() + " for share link " + id);
				return JsonParser.parseString(response.content()).getAsJsonObject().get("path").getAsString();
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);
	}

	/**
	 * Decodes the first plot with crops of a long link (or its path or code) into a target greenhouse grid, indexed
	 * {@code [x][z]} from the north west corner. Cells are -1 where the layout says nothing, 0 for a mutation spot, or a crop id.
	 *
	 * @return the grid, or null if the link isn't a SkyLayouts layout with crops
	 */
	public static int @Nullable [][] decode(String link) {
		try {
			@Nullable String[][] board = firstBoard(link.strip());
			return board == null ? null : toTarget(board);
		} catch (Exception _) {
			return null;
		}
	}

	private static @Nullable String @Nullable [][] firstBoard(String link) throws IOException {
		int path = link.indexOf("/l/");
		if (path >= 0) return fromCode(URLDecoder.decode(cutAtAny(link.substring(path + "/l/".length()), "?#/"), StandardCharsets.UTF_8));

		int fragment = link.indexOf('#');
		if (fragment >= 0) return fromFragment(link.substring(fragment + 1));

		return fromCode(link);
	}

	private static String cutAtAny(String string, String delimiters) {
		for (int i = 0; i < string.length(); i++) {
			if (delimiters.indexOf(string.charAt(i)) >= 0) return string.substring(0, i);
		}
		return string;
	}

	private static @Nullable String @Nullable [][] fromCode(String code) throws IOException {
		if (code.length() > MAX_CODE_LENGTH || code.length() < 3 || code.charAt(0) != '1') return null;
		if (digit(code.charAt(1)) < 0 || digit(code.charAt(2)) < 0) return null;

		int boardsStart = code.indexOf('~');
		if (boardsStart < 0) return null;
		String boards = code.substring(boardsStart + 1);

		if (boards.startsWith("p")) {
			String[] plots = boards.substring(1).split("~", -1);
			for (int plot = 0; plot < Math.min(plots.length, PLOTS); plot++) {
				// Plots copying an earlier plot can be skipped, since that plot would be picked first
				@Nullable String[][] board = unpack(plots[plot]);
				if (board != null && hasCrop(board)) return board;
			}
			return null;
		}
		if (boards.startsWith("z")) return inflateBoards(boards.substring(1));
		return null;
	}

	private static @Nullable String @Nullable [][] fromFragment(String fragment) {
		Map<String, String> params = new HashMap<>();
		for (String param : fragment.split("&")) {
			int equals = param.indexOf('=');
			if (equals > 0) params.put(param.substring(0, equals), param.substring(equals + 1));
		}

		for (int plot = 1; plot <= PLOTS; plot++) {
			String packed = params.get("b" + plot);
			if (packed == null) continue;

			@Nullable String[][] board = unpack(URLDecoder.decode(packed, StandardCharsets.UTF_8));
			if (board != null && hasCrop(board)) return board;
		}
		return null;
	}

	private static @Nullable String @Nullable [][] unpack(String packed) {
		if (packed.length() < 3 || packed.charAt(0) != '3') return null;

		int size = digit(packed.charAt(1));
		int kindCount = digit(packed.charAt(2));
		if (size != GRID_SIZE || kindCount < 0) return null;

		int[] kinds = parseKinds(packed, 3, kindCount);
		if (kinds == null) return null;

		BigInteger number = BigInteger.ZERO;
		for (int i = 3 + kindCount; i < packed.length(); i++) {
			int digit = digit(packed.charAt(i));
			if (digit < 0) return null;
			number = number.shiftLeft(6).or(BigInteger.valueOf(digit));
		}

		BigInteger base = BigInteger.valueOf(kindCount + 1);
		int[] cells = new int[TOTAL_CELLS];
		for (int cell = TOTAL_CELLS - 1; cell >= 0; cell--) {
			BigInteger[] quotientAndRemainder = number.divideAndRemainder(base);
			cells[cell] = quotientAndRemainder[1].intValueExact();
			number = quotientAndRemainder[0];
		}
		// Only the leading 1 may be left once every cell is read
		if (!number.equals(BigInteger.ONE)) return null;

		return toBoard(cells, kinds);
	}

	private static @Nullable String @Nullable [][] inflateBoards(String data) throws IOException {
		if (data.length() < 3) return null;

		int size = digit(data.charAt(0));
		int plotMask = digit(data.charAt(1));
		int kindCount = digit(data.charAt(2));
		if (size != GRID_SIZE || plotMask < 1 || plotMask >= 1 << PLOTS || kindCount < 0) return null;

		int[] kinds = parseKinds(data, 3, kindCount);
		if (kinds == null) return null;

		int plots = Integer.bitCount(plotMask);
		byte[] cells = inflate(Base64.getUrlDecoder().decode(data.substring(3 + kindCount)), plots * TOTAL_CELLS);
		if (cells == null) return null;

		// Only the plots in the mask are stored, in order of their number
		for (int plot = 0; plot < plots; plot++) {
			int[] plotCells = new int[TOTAL_CELLS];
			for (int cell = 0; cell < TOTAL_CELLS; cell++) {
				plotCells[cell] = Byte.toUnsignedInt(cells[plot * TOTAL_CELLS + cell]);
			}

			@Nullable String[][] board = toBoard(plotCells, kinds);
			if (board != null && hasCrop(board)) return board;
		}
		return null;
	}

	private static int @Nullable [] parseKinds(String string, int start, int count) {
		if (string.length() < start + count) return null;

		int[] kinds = new int[count];
		for (int i = 0; i < count; i++) {
			kinds[i] = digit(string.charAt(start + i));
			if (kinds[i] < 0 || kinds[i] >= KINDS.length) return null;
		}
		return kinds;
	}

	private static byte @Nullable [] inflate(byte[] compressed, int length) throws IOException {
		// Raw deflate, no zlib header
		Inflater inflater = new Inflater(true);
		try (InputStream in = new InflaterInputStream(new ByteArrayInputStream(compressed), inflater)) {
			byte[] decompressed = in.readNBytes(length);
			if (decompressed.length != length || in.read() != -1) return null;
			return decompressed;
		} finally {
			inflater.end();
		}
	}

	/**
	 * @param cells 0 for empty, or the index into {@code kinds} plus one, row major
	 * @return the crop names in screen space, indexed {@code [row][col]} and null where there is no crop
	 */
	private static @Nullable String @Nullable [][] toBoard(int[] cells, int[] kinds) {
		@Nullable String[][] board = new String[GRID_SIZE][GRID_SIZE];
		for (int cell = 0; cell < TOTAL_CELLS; cell++) {
			if (cells[cell] == 0) continue;
			if (cells[cell] > kinds.length) return null;

			board[cell / GRID_SIZE][cell % GRID_SIZE] = KINDS[kinds[cells[cell] - 1]];
		}
		return board;
	}

	private static boolean hasCrop(@Nullable String[][] board) {
		for (@Nullable String[] row : board) {
			for (@Nullable String crop : row) {
				if (crop != null) return true;
			}
		}
		return false;
	}

	private static int[][] toTarget(@Nullable String[][] board) {
		int[][] target = new int[GRID_SIZE][GRID_SIZE];
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				String crop = board[row][col];
				int value = crop != null ? Objects.requireNonNull(GreenhouseCrops.CROP_ID_MAP.get(crop), crop).id()
						: isMutationSpot(board, row, col) ? 0 : -1;

				// The top of the layout (where SkyLayouts shows the carpenter) faces north, so rows run along +z and columns along +x
				target[col][row] = value;
			}
		}
		return target;
	}

	private static boolean isMutationSpot(@Nullable String[][] board, int row, int col) {
		Map<String, Integer> neighbours = new HashMap<>();
		for (int r = Math.max(row - 1, 0); r <= Math.min(row + 1, GRID_SIZE - 1); r++) {
			for (int c = Math.max(col - 1, 0); c <= Math.min(col + 1, GRID_SIZE - 1); c++) {
				String crop = board[r][c];
				if (crop != null && (r != row || c != col)) neighbours.merge(crop, 1, Integer::sum);
			}
		}
		if (neighbours.isEmpty()) return false;

		return MUTATION_RECIPES.stream().anyMatch(recipe -> recipe.entrySet().stream()
				.allMatch(needed -> neighbours.getOrDefault(needed.getKey(), 0) >= needed.getValue()));
	}

	private static int digit(char c) {
		return DIGITS.indexOf(c);
	}
}
