package de.hysky.skyblocker.skyblock.garden;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Http;

/**
 * Loads greenhouse layouts from <a href="https://skylayouts.io">SkyLayouts</a>, whose creator is fine with us using its API.
 *
 * <p>Layout links come in these shapes:
 * <ul>
 *     <li>A short link ({@code /l/s} and 16 characters) only holds an id, which the site's API turns into one of the others.</li>
 *     <li>A long link ({@code /l/1<header>~<boards>}) holds its boards.</li>
 *     <li>{@code /layout.html?m=<mutation>&i=<interval>} shows boards bundled with the site, named by the {@code ranked}
 *     parameter or else picked from the mutation and interval. They are looked up in the site's data files, which also say
 *     where mutations spawn. Boards edited on the site come in the fragment, as in {@code #b1=<board>}, and replace the
 *     bundled board of their plot.</li>
 * </ul>
 *
 * <p>The long link header is {@code 1<mutation><interval><flags>}, where a {@code 2} or {@code 3} flag is the plot that
 * was showing. The boards are either
 * <ul>
 *     <li>{@code p<plot 1>~<plot 2>~<plot 3>}, each plot empty, a digit copying an earlier plot, or a packed board, or</li>
 *     <li>{@code z<size><plot mask><kind count><kinds><data>}, where the data is the base64url encoded, raw deflated cells
 *     of every plot in the mask, one byte per cell holding 0 for empty or the index into the kinds plus one.</li>
 * </ul>
 *
 * <p>A packed board is {@code 3<size><kind count><kinds><number>}, where the base 64 number is a 1 followed by one digit
 * in base {@code kind count + 1} per cell, 0 for empty. Single characters above are base64url digits, kinds index
 * {@link #KINDS}, and cells run row major from the top left. Crops bigger than one cell fill every cell they cover.
 *
 * <p>Only bundled boards say where mutations spawn, so for the others {@link #MUTATION_RECIPES} marks the empty cells a
 * mutation would spawn on.
 */
public final class SkyLayoutsLayout {
	private static final String SITE = "https://skylayouts.io";
	private static final Pattern SHORT_LINK = Pattern.compile("/l/(s[A-Za-z0-9_-]{16})(?![A-Za-z0-9_~-])");
	private static final Pattern MUTATION = Pattern.compile("[A-Za-z0-9_]{1,64}");
	private static final Pattern VARIANT = Pattern.compile("[A-Za-z0-9_-]{1,24}");
	private static final Pattern DATA_VERSION = Pattern.compile("[A-Za-z0-9._-]{1,64}");
	private static final String DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
	private static final int GRID_SIZE = 10;
	private static final int TOTAL_CELLS = GRID_SIZE * GRID_SIZE;
	private static final int PLOTS = 3;
	private static final int MAX_CODE_LENGTH = 4096;
	// The site shows the boards of these mutation variants under the mutation itself
	private static final Map<String, String> MUTATION_ALIASES = Map.of("BLASTBERRY_PLAIN", "BLASTBERRY", "SHELLFRUIT_NOTECH", "SHELLFRUIT");

	/**
	 * SkyLayouts' kinds in the order its codes index them, with their {@link GreenhouseCrops} name. The ground blocks at the
	 * end aren't crops.
	 */
	static final List<Kind> KINDS = List.of(
			new Kind("ALL_IN_ALOE", "All In Aloe"), new Kind("ASHWREATH", "Ashwreath"), new Kind("BLASTBERRY", "Blastberry"),
			new Kind("BROWN_MUSHROOM", "Brown Mushroom"), new Kind("CACTUS", "Cactus"), new Kind("CARROT", "Carrot"),
			new Kind("CHEESEBITE", "Cheesebite"), new Kind("CHLORONITE", "Chloronite"), new Kind("CHOCOBERRY", "Chocoberry"),
			new Kind("CHOCONUT", "Choconut"), new Kind("CHORUS_FRUIT", "Chorus Fruit"), new Kind("CINDERSHADE", "Cindershade"),
			new Kind("COALROOT", "Coalroot"), new Kind("COCOA_BEANS", "Cocoa Beans"), new Kind("CREAMBLOOM", "Creambloom"),
			new Kind("DEAD_PLANT", "Dead Plants"), new Kind("DEVOURER", "Devourer"), new Kind("DO_NOT_EAT_SHROOM", "Do Not Eat Shroom"),
			new Kind("DUSKBLOOM", "Duskbloom"), new Kind("DUSTGRAIN", "Dustgrain"), new Kind("FERMENTO", "Fermento"),
			new Kind("FIRE", "Fire"), new Kind("FLESHTRAP", "Fleshtrap"), new Kind("GLASSCORN", "Glasscorn"),
			new Kind("GLOOMGOURD", "Gloomgourd"), new Kind("GODSEED", "Godseed"), new Kind("LONELILY", "Lonelily"),
			new Kind("MAGIC_JELLYBEAN", "Magic Jellybean"), new Kind("MELON", "Melon Seeds"), new Kind("MOONFLOWER", "Moonflower"),
			new Kind("NETHER_WART", "Nether Wart"), new Kind("NOCTILUME", "Noctilume"), new Kind("PHANTOMLEAF", "Phantomleaf"),
			new Kind("PLANTBOY_ADVANCE", "PlantBoy Advance"), new Kind("POTATO", "Potato"), new Kind("PUFFERCLOUD", "Puffercloud"),
			new Kind("PUMPKIN", "Pumpkin Seeds"), new Kind("RED_MUSHROOM", "Red Mushroom"), new Kind("SCOURROOT", "Scourroot"),
			new Kind("SHADEVINE", "Shadevine"), new Kind("SHELLFRUIT", "Shellfruit"), new Kind("SNOOZLING", "Snoozling"),
			new Kind("SOGGYBUD", "Soggybud"), new Kind("STARTLEVINE", "Startlevine"), new Kind("STOPLIGHT_PETAL", "Stoplight Petal"),
			new Kind("SUGAR_CANE", "Sugar Cane"), new Kind("SUNFLOWER", "Sunflower"), new Kind("THORNSHADE", "Thornshade"),
			new Kind("THUNDERLING", "Thunderling"), new Kind("TIMESTALK", "Timestalk"), new Kind("TURTLELLINI", "Turtlellini"),
			new Kind("VEILSHROOM", "Veilshroom"), new Kind("WHEAT", "Wheat Seeds"), new Kind("WILD_ROSE", "Wild Rose"),
			new Kind("WITHERBLOOM", "Witherbloom"), new Kind("ZOMBUD", "Zombud"),
			new Kind("FARMLAND", null), new Kind("SAND", null), new Kind("SOUL_SAND", null),
			new Kind("END_STONE", null), new Kind("DIRT", null), new Kind("MYCELIUM", null)
	);
	private static final Map<String, Kind> KINDS_BY_ID = KINDS.stream().collect(Collectors.toUnmodifiableMap(Kind::id, Function.identity()));

	/**
	 * How mutations spawn: on an empty {@code size} by {@code size} area whose surrounding cells hold at least these crops,
	 * counting every cell a crop covers, or with no crops at all around it for Lonelily. These match the spots the site lists
	 * for its bundled boards. Godseed and Shellfruit spawn by other rules, so their spots aren't marked.
	 */
	static final List<Recipe> MUTATION_RECIPES = List.of(
			new Recipe(1, Map.of("Magic Jellybean", 6, "PlantBoy Advance", 2)), // All In Aloe
			new Recipe(1, Map.of("Fire", 2, "Nether Wart", 2)), // Ashwreath
			new Recipe(1, Map.of("Ashwreath", 3, "Chocoberry", 5)), // Blastberry
			new Recipe(1, Map.of("Creambloom", 4, "Fermento", 4)), // Cheesebite
			new Recipe(1, Map.of("Coalroot", 6, "Thornshade", 2)), // Chloronite
			new Recipe(1, Map.of("Choconut", 6, "Gloomgourd", 2)), // Chocoberry
			new Recipe(1, Map.of("Cocoa Beans", 2)), // Choconut
			new Recipe(1, Map.of("Chloronite", 5, "Magic Jellybean", 3)), // Chorus Fruit
			new Recipe(1, Map.of("Ashwreath", 4, "Witherbloom", 4)), // Cindershade
			new Recipe(1, Map.of("Ashwreath", 5, "Scourroot", 3)), // Coalroot
			new Recipe(1, Map.of("Choconut", 8)), // Creambloom
			new Recipe(1, Map.of("Puffercloud", 4, "Zombud", 4)), // Devourer
			new Recipe(1, Map.of("Scourroot", 4, "Veilshroom", 4)), // Do Not Eat Shroom
			new Recipe(1, Map.of("Dustgrain", 2, "Moonflower", 2, "Shadevine", 2, "Sunflower", 2)), // Duskbloom
			new Recipe(1, Map.of("Wheat Seeds", 2)), // Dustgrain
			new Recipe(1, Map.of("Cindershade", 4, "Lonelily", 4)), // Fleshtrap
			new Recipe(2, Map.of("Chloronite", 6, "Startlevine", 6)), // Glasscorn
			new Recipe(1, Map.of("Melon Seeds", 1, "Pumpkin Seeds", 1)), // Gloomgourd
			new Recipe(1, Map.of()), // Lonelily
			new Recipe(1, Map.of("Duskbloom", 3, "Sugar Cane", 5)), // Magic Jellybean
			new Recipe(2, Map.of("Duskbloom", 6, "Lonelily", 6)), // Noctilume
			new Recipe(1, Map.of("Chorus Fruit", 4, "Shellfruit", 4)), // Phantomleaf
			new Recipe(2, Map.of("Snoozling", 6, "Thunderling", 6)), // PlantBoy Advance
			new Recipe(1, Map.of("Do Not Eat Shroom", 6, "Snoozling", 2)), // Puffercloud
			new Recipe(1, Map.of("Carrot", 1, "Potato", 1)), // Scourroot
			new Recipe(1, Map.of("Cactus", 1, "Sugar Cane", 1)), // Shadevine
			new Recipe(3, Map.of("Creambloom", 4, "Duskbloom", 3, "Dustgrain", 3, "Thornshade", 3, "Witherbloom", 3)), // Snoozling
			new Recipe(1, Map.of("Gloomgourd", 2, "Melon Seeds", 2)), // Soggybud
			new Recipe(1, Map.of("Blastberry", 4, "Cheesebite", 4)), // Startlevine
			new Recipe(1, Map.of("Noctilume", 4, "Snoozling", 4)), // Stoplight Petal
			new Recipe(1, Map.of("Veilshroom", 4, "Wild Rose", 4)), // Thornshade
			new Recipe(1, Map.of("Noctilume", 3, "Soggybud", 5)), // Thunderling
			new Recipe(1, Map.of("Chorus Fruit", 2, "Shellfruit", 2, "Stoplight Petal", 4)), // Timestalk
			new Recipe(1, Map.of("Choconut", 4, "Soggybud", 4)), // Turtlellini
			new Recipe(1, Map.of("Brown Mushroom", 1, "Red Mushroom", 1)), // Veilshroom
			new Recipe(1, Map.of("Dead Plants", 4)), // Witherbloom
			new Recipe(1, Map.of("Cindershade", 2, "Dead Plants", 4, "Fleshtrap", 2)) // Zombud
	);

	private SkyLayoutsLayout() {
	}

	/**
	 * Loads the layout behind any SkyLayouts link, looking up short links and bundled boards through the site's API.
	 * Uses the plot the link was showing, or else the first plot with crops.
	 *
	 * @return the target greenhouse grid as {@link #decode(String)} describes it, or null if the link isn't a SkyLayouts layout with crops
	 */
	public static CompletableFuture<int @Nullable [][]> load(String link) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return loadNow(link.strip());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new CompletionException(e);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);
	}

	private static int @Nullable [][] loadNow(String link) throws IOException, InterruptedException {
		String shortLinkId = shortLinkId(link);
		if (shortLinkId != null) link = resolveShortLink(shortLinkId);

		List<List<String>> bundledKeys = bundledBoardKeys(link);
		if (bundledKeys.isEmpty()) return decode(link);

		List<@Nullable Board> plots = fragmentBoards(link);
		Set<String> wanted = new HashSet<>();
		for (int plot = 0; plot < PLOTS; plot++) {
			if (plots.get(plot) == null) wanted.addAll(bundledKeys.get(plot));
		}
		Map<String, JsonObject> bundled = fetchBundledBoards(wanted);

		for (int plot = 0; plot < PLOTS; plot++) {
			if (plots.get(plot) != null) continue;

			// The keys are in the order the site tries them
			for (String key : bundledKeys.get(plot)) {
				JsonObject json = bundled.get(key);
				if (json == null) continue;

				plots.set(plot, fromBundledBoard(json));
				break;
			}
		}

		Board board = choosePlot(plots, viewedPlot(query(link)));
		return board == null ? null : toTarget(board);
	}

	/**
	 * @return the id of a short link, or null if the link isn't one
	 */
	static @Nullable String shortLinkId(String link) {
		Matcher matcher = SHORT_LINK.matcher(link);
		return matcher.find() ? matcher.group(1) : null;
	}

	/**
	 * @return the path of the link behind a short link
	 */
	private static String resolveShortLink(String id) throws IOException, InterruptedException {
		try (Http.ApiResponse response = Http.sendCacheableGetRequest(SITE + "/api/share-links/" + id, null)) {
			if (!response.ok()) throw new IOException("SkyLayouts returned status " + response.statusCode() + " for share link " + id);
			return JsonParser.parseString(response.content()).getAsJsonObject().get("path").getAsString();
		}
	}

	/**
	 * Decodes a long link (or its path or code), or a {@code /layout.html} link with boards in its fragment, into a target
	 * greenhouse grid. Uses the plot the link was showing, or else the first plot with crops.
	 *
	 * @return the grid, indexed {@code [x][z]} from the north west corner, holding -1 where the layout says nothing, 0 for a
	 * mutation spot, or a crop id. Null if the link isn't a SkyLayouts layout with crops.
	 */
	public static int @Nullable [][] decode(String link) {
		try {
			Board board = decodeBoard(link.strip());
			return board == null ? null : toTarget(board);
		} catch (Exception _) {
			return null;
		}
	}

	/**
	 * Decodes a board bundled with the site, as found in its data files.
	 *
	 * @return the target grid as {@link #decode(String)} describes it, or null if the board isn't one this can paste
	 */
	static int @Nullable [][] decodeBundledBoard(JsonObject json) {
		try {
			Board board = fromBundledBoard(json);
			return board == null ? null : toTarget(board);
		} catch (Exception _) {
			return null;
		}
	}

	private static @Nullable Board decodeBoard(String link) throws IOException {
		int path = link.indexOf("/l/");
		if (path >= 0) return fromCode(URLDecoder.decode(cutAtAny(link.substring(path + "/l/".length()), "?#/"), StandardCharsets.UTF_8));

		if (link.indexOf('#') >= 0) return choosePlot(fragmentBoards(link), viewedPlot(query(link)));

		return fromCode(link);
	}

	private static String cutAtAny(String string, String delimiters) {
		for (int i = 0; i < string.length(); i++) {
			if (delimiters.indexOf(string.charAt(i)) >= 0) return string.substring(0, i);
		}
		return string;
	}

	private static @Nullable Board fromCode(String code) throws IOException {
		if (code.length() > MAX_CODE_LENGTH || code.length() < 3 || code.charAt(0) != '1') return null;
		if (digit(code.charAt(1)) < 0 || digit(code.charAt(2)) < 0) return null;

		int boardsStart = code.indexOf('~');
		if (boardsStart < 0) return null;
		String flags = code.substring(3, boardsStart);
		int viewedPlot = flags.contains("2") ? 2 : flags.contains("3") ? 3 : 1;
		String boards = code.substring(boardsStart + 1);

		List<@Nullable Board> plots = emptyPlots();
		if (boards.startsWith("p")) {
			String[] entries = boards.substring(1).split("~", -1);
			for (int plot = 0; plot < Math.min(entries.length, PLOTS); plot++) {
				String entry = entries[plot];
				int copied = entry.equals("1") || entry.equals("2") ? entry.charAt(0) - '1' : -1;

				@Nullable String[][] crops = copied >= 0 ? null : unpack(entry);
				if (copied >= 0 && copied < plot) plots.set(plot, plots.get(copied));
				else if (crops != null) plots.set(plot, new Board(crops, null));
			}
		} else if (boards.startsWith("z")) {
			if (!inflateBoards(boards.substring(1), plots)) return null;
		} else {
			return null;
		}
		return choosePlot(plots, viewedPlot);
	}

	private static List<@Nullable Board> fragmentBoards(String link) {
		Map<String, String> fragment = fragment(link);
		int viewedPlot = viewedPlot(query(link));
		List<@Nullable Board> plots = emptyPlots();
		for (int plot = 0; plot < PLOTS; plot++) {
			String packed = fragment.get("b" + (plot + 1));
			// Older links put the board of the plot that was showing in b
			if (packed == null && plot + 1 == viewedPlot) packed = fragment.get("b");

			@Nullable String[][] crops = packed == null ? null : unpackFragmentBoard(packed);
			if (crops != null) plots.set(plot, new Board(crops, null));
		}
		return plots;
	}

	/**
	 * Reads a board from the fragment. Besides packed boards, older links used {@code 2~<size>~<kinds>~<cells>}, with
	 * comma separated kinds and each cell a base 36 index into them, or {@code .} and a base 36 count of empty cells. The
	 * oldest used {@code <kind>=<cells>|...}, with each cell a base 36 column then row.
	 */
	private static @Nullable String @Nullable [][] unpackFragmentBoard(String board) {
		if (board.length() > MAX_CODE_LENGTH) return null;
		if (board.startsWith("3")) return unpack(board);

		@Nullable String[][] crops = new String[GRID_SIZE][GRID_SIZE];
		if (board.startsWith("2~")) {
			String[] parts = board.split("~", -1);
			// The size is base 36, and left out for the default of 10
			if (!parts[1].isEmpty() && !parts[1].equalsIgnoreCase("a")) return null;

			String[] kinds = parts.length > 2 && !parts[2].isEmpty() ? parts[2].split(",") : new String[0];
			String cells = parts.length > 3 ? parts[3] : "";
			int cell = 0;
			for (int i = 0; i < cells.length() && cell < TOTAL_CELLS; i++) {
				if (cells.charAt(i) == '.') {
					cell += i + 1 < cells.length() ? Math.max(Character.digit(cells.charAt(i + 1), 36), 0) : 0;
					i++;
					continue;
				}

				int kind = Character.digit(cells.charAt(i), 36);
				if (kind >= 0 && kind < kinds.length) crops[cell / GRID_SIZE][cell % GRID_SIZE] = cropOf(kinds[kind]);
				cell++;
			}
			return crops;
		}

		for (String part : board.split("\\|")) {
			int equals = part.indexOf('=');
			String crop = equals < 0 ? null : cropOf(part.substring(0, equals));
			if (crop == null) continue;

			String cells = part.substring(equals + 1);
			for (int i = 0; i + 1 < cells.length(); i += 2) {
				int x = Character.digit(cells.charAt(i), 36);
				int y = Character.digit(cells.charAt(i + 1), 36);
				if (inGrid(x, y)) crops[y][x] = crop;
			}
		}
		return crops;
	}

	/**
	 * @return the {@link GreenhouseCrops} name of a SkyLayouts kind, or null for ground blocks and kinds this doesn't know
	 */
	private static @Nullable String cropOf(String id) {
		Kind kind = KINDS_BY_ID.get("MUSHROOM".equals(id) ? "RED_MUSHROOM" : id);
		return kind == null ? null : kind.crop();
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

		return toCrops(cells, kinds);
	}

	/**
	 * @return whether the data could be read, in which case the plots in its mask are set
	 */
	private static boolean inflateBoards(String data, List<@Nullable Board> plots) throws IOException {
		if (data.length() < 3) return false;

		int size = digit(data.charAt(0));
		int plotMask = digit(data.charAt(1));
		int kindCount = digit(data.charAt(2));
		if (size != GRID_SIZE || plotMask < 1 || plotMask >= 1 << PLOTS || kindCount < 0) return false;

		int[] kinds = parseKinds(data, 3, kindCount);
		if (kinds == null) return false;

		byte[] cells = inflate(Base64.getUrlDecoder().decode(data.substring(3 + kindCount)), Integer.bitCount(plotMask) * TOTAL_CELLS);
		if (cells == null) return false;

		// Only the plots in the mask are stored, in order of their number
		int stored = 0;
		for (int plot = 0; plot < PLOTS; plot++) {
			if ((plotMask & 1 << plot) == 0) continue;

			int[] plotCells = new int[TOTAL_CELLS];
			for (int cell = 0; cell < TOTAL_CELLS; cell++) {
				plotCells[cell] = Byte.toUnsignedInt(cells[stored * TOTAL_CELLS + cell]);
			}
			stored++;

			@Nullable String[][] crops = toCrops(plotCells, kinds);
			if (crops == null) return false;
			plots.set(plot, new Board(crops, null));
		}
		return true;
	}

	private static int @Nullable [] parseKinds(String string, int start, int count) {
		if (string.length() < start + count) return null;

		int[] kinds = new int[count];
		for (int i = 0; i < count; i++) {
			kinds[i] = digit(string.charAt(start + i));
			if (kinds[i] < 0 || kinds[i] >= KINDS.size()) return null;
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
	private static @Nullable String @Nullable [][] toCrops(int[] cells, int[] kinds) {
		@Nullable String[][] crops = new String[GRID_SIZE][GRID_SIZE];
		for (int cell = 0; cell < TOTAL_CELLS; cell++) {
			if (cells[cell] == 0) continue;
			if (cells[cell] > kinds.length) return null;

			crops[cell / GRID_SIZE][cell % GRID_SIZE] = KINDS.get(kinds[cells[cell] - 1]).crop();
		}
		return crops;
	}

	/**
	 * Lists the bundled boards a {@code /layout.html} link shows on each plot, as the keys of the site's data files.
	 *
	 * @return for each plot, the keys to try in order, or an empty list if the link doesn't show bundled boards
	 */
	static List<List<String>> bundledBoardKeys(String link) {
		Map<String, String> query = query(link);
		String mutation = query.get("m");
		int interval = positiveInt(query.get("i"));
		if (mutation == null || !MUTATION.matcher(mutation).matches() || mutation.equals("blank") || interval <= 0) return List.of();

		mutation = MUTATION_ALIASES.getOrDefault(mutation.toUpperCase(Locale.ENGLISH), mutation).toLowerCase(Locale.ENGLISH);
		List<List<String>> ranked = rankedKeys(query.get("ranked"), mutation, interval, positiveInt(query.get("ri")));
		if (ranked != null) return ranked;

		// Without ranked boards the site shows its own board for the mutation and interval
		String prefix = mutation + "_s" + interval;
		String layout = "collection".equals(query.get("l")) ? "_c" : "";
		String variant = query.get("v");
		boolean hasVariant = variant != null && VARIANT.matcher(variant).matches();

		List<List<String>> plots = new ArrayList<>();
		for (int plot = 1; plot <= PLOTS; plot++) {
			Set<String> keys = new LinkedHashSet<>();
			if (hasVariant) {
				keys.add(prefix + layout + "_plot" + plot + "__" + variant);
				keys.add(prefix + "_plot" + plot + "__" + variant);
			}
			keys.add(prefix + layout + "_plot" + plot);
			keys.add(prefix + "_plot" + plot);
			if (plot == 1) keys.add(prefix);
			plots.add(List.copyOf(keys));
		}
		return plots;
	}

	/**
	 * @param source the interval chorus fruit boards were ranked at, or a non positive number
	 * @return for each plot, the ranked key in a list of its own, or null if the site would ignore these ranked boards
	 */
	private static @Nullable List<List<String>> rankedKeys(@Nullable String ranked, String mutation, int interval, int source) {
		if (ranked == null || ranked.isEmpty()) return null;

		String[] keys = ranked.split(",", -1);
		if (keys.length > PLOTS || Arrays.stream(keys).allMatch(String::isEmpty)) return null;

		String prefix = mutation + "_s" + (source > 0 ? source : interval);
		List<List<String>> plots = new ArrayList<>();
		for (int plot = 1; plot <= PLOTS; plot++) {
			String key = plot <= keys.length ? keys[plot - 1] : "";
			if (key.isEmpty()) {
				plots.add(List.of());
				continue;
			}

			// The site only shows ranked boards of the linked mutation and interval, for the plot they are listed at
			boolean valid = plot == 1 && key.equals(prefix) || key.startsWith(prefix + "_")
					&& key.substring(prefix.length() + 1).matches("(?i)(?:c_)?(?:p[12]_)?plot" + plot + "(?:__[a-z0-9_-]{1,24})?");
			if (!valid) return null;
			plots.add(List.of(key));
		}
		return plots;
	}

	/**
	 * @return the data file of the site holding the bundled board with this key
	 */
	static String dataFileOf(String key) {
		if (key.matches(".*_s\\d+_c(?:_.*)?")) return "boards-collection.json";
		if (key.startsWith("devourer_")) return "boards-devourer.json";
		if (key.matches(".*_s\\d+_p[12]_.*")) return "boards-plots.json";
		return "boards.json";
	}

	private static Map<String, JsonObject> fetchBundledBoards(Set<String> keys) throws IOException, InterruptedException {
		if (keys.isEmpty()) return Map.of();

		String version = JsonParser.parseString(Http.sendGetRequest(SITE + "/data/latest.json")).getAsJsonObject().get("version").getAsString();
		if (!DATA_VERSION.matcher(version).matches()) throw new IOException("Unexpected SkyLayouts data version " + version);

		Map<String, Set<String>> keysByFile = keys.stream().collect(Collectors.groupingBy(SkyLayoutsLayout::dataFileOf, Collectors.toSet()));
		Map<String, JsonObject> boards = new HashMap<>();
		for (Map.Entry<String, Set<String>> file : keysByFile.entrySet()) {
			// The files are megabytes of boards, so only the wanted ones are kept
			try (JsonReader reader = new JsonReader(new InputStreamReader(Http.downloadContent(SITE + "/data/" + version + "/" + file.getKey()), StandardCharsets.UTF_8))) {
				reader.beginObject();
				while (reader.hasNext()) {
					String key = reader.nextName();
					if (file.getValue().contains(key)) {
						boards.put(key, JsonParser.parseReader(reader).getAsJsonObject());
					} else {
						reader.skipValue();
					}
				}
			}
		}
		return boards;
	}

	/**
	 * Reads a bundled board: {@code tiles} lists every cell a crop covers as {@code {x, y, kind}}, and {@code spots} lists
	 * where mutations spawn as the top left {@code {x, y}} of a {@code footprint} sized area.
	 */
	private static @Nullable Board fromBundledBoard(JsonObject json) {
		if (json.has("size") && json.get("size").getAsInt() != GRID_SIZE) return null;

		@Nullable String[][] crops = new String[GRID_SIZE][GRID_SIZE];
		JsonArray tiles = json.getAsJsonArray("tiles");
		if (tiles == null) return null;
		for (JsonElement element : tiles) {
			JsonObject tile = element.getAsJsonObject();
			int x = tile.get("x").getAsInt();
			int y = tile.get("y").getAsInt();
			String crop = cropOf(tile.get("kind").getAsString());

			// Ground blocks aren't crops, and kinds added to the site later are left out
			if (crop != null && inGrid(x, y)) crops[y][x] = crop;
		}

		JsonArray footprint = json.getAsJsonArray("footprint");
		int width = footprint == null ? 1 : footprint.get(0).getAsInt();
		int height = footprint == null ? 1 : footprint.get(1).getAsInt();

		boolean[][] spots = new boolean[GRID_SIZE][GRID_SIZE];
		JsonArray spotList = json.getAsJsonArray("spots");
		for (JsonElement element : spotList == null ? new JsonArray() : spotList) {
			JsonObject spot = element.getAsJsonObject();
			int x = spot.get("x").getAsInt();
			int y = spot.get("y").getAsInt();
			for (int dy = 0; dy < height; dy++) {
				for (int dx = 0; dx < width; dx++) {
					if (inGrid(x + dx, y + dy)) spots[y + dy][x + dx] = true;
				}
			}
		}
		return new Board(crops, spots);
	}

	private static boolean inGrid(int x, int y) {
		return x >= 0 && y >= 0 && x < GRID_SIZE && y < GRID_SIZE;
	}

	private static List<@Nullable Board> emptyPlots() {
		List<@Nullable Board> plots = new ArrayList<>();
		for (int plot = 0; plot < PLOTS; plot++) plots.add(null);
		return plots;
	}

	/**
	 * @param viewedPlot the plot the link was showing, from 1
	 */
	private static @Nullable Board choosePlot(List<@Nullable Board> plots, int viewedPlot) {
		Board viewed = plots.get(viewedPlot - 1);
		if (viewed != null && viewed.hasCrop()) return viewed;

		for (Board board : plots) {
			if (board != null && board.hasCrop()) return board;
		}
		return null;
	}

	private static int viewedPlot(Map<String, String> query) {
		return Math.clamp(positiveInt(query.get("p")), 1, PLOTS);
	}

	/**
	 * @return the number, or -1 if it isn't a positive whole number
	 */
	private static int positiveInt(@Nullable String number) {
		if (number == null) return -1;
		try {
			int parsed = Integer.parseInt(number);
			return parsed > 0 ? parsed : -1;
		} catch (NumberFormatException _) {
			return -1;
		}
	}

	private static Map<String, String> query(String link) {
		int start = link.indexOf('?');
		int fragment = link.indexOf('#');
		if (start < 0 || fragment >= 0 && fragment < start) return Map.of();
		return params(link.substring(start + 1, fragment >= 0 ? fragment : link.length()));
	}

	private static Map<String, String> fragment(String link) {
		int start = link.indexOf('#');
		return start < 0 ? Map.of() : params(link.substring(start + 1));
	}

	// Like the site, only the first value of a repeated parameter counts
	private static Map<String, String> params(String params) {
		Map<String, String> parsed = new HashMap<>();
		for (String param : params.split("&")) {
			int equals = param.indexOf('=');
			if (equals > 0) parsed.putIfAbsent(param.substring(0, equals), URLDecoder.decode(param.substring(equals + 1), StandardCharsets.UTF_8));
		}
		return parsed;
	}

	private static int[][] toTarget(Board board) {
		boolean[][] spots = board.spots() != null ? board.spots() : inferSpots(board.crops());
		int[][] target = new int[GRID_SIZE][GRID_SIZE];
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				String crop = board.crops()[row][col];
				int value = crop != null ? Objects.requireNonNull(GreenhouseCrops.CROP_ID_MAP.get(crop), crop).id() : spots[row][col] ? 0 : -1;

				// The top of the layout (where SkyLayouts shows the carpenter) faces north, so rows run along +z and columns along +x
				target[col][row] = value;
			}
		}
		return target;
	}

	/**
	 * @return the cells a mutation would spawn on, indexed like {@code crops}
	 */
	private static boolean[][] inferSpots(@Nullable String[][] crops) {
		boolean[][] spots = new boolean[GRID_SIZE][GRID_SIZE];
		for (Recipe recipe : MUTATION_RECIPES) {
			int size = recipe.size();
			for (int row = 0; row + size <= GRID_SIZE; row++) {
				for (int col = 0; col + size <= GRID_SIZE; col++) {
					if (!spawns(recipe, crops, row, col)) continue;

					for (int r = row; r < row + size; r++) {
						Arrays.fill(spots[r], col, col + size, true);
					}
				}
			}
		}
		return spots;
	}

	/**
	 * @return whether the mutation spawns on the area with its top left cell at {@code row} and {@code col}
	 */
	private static boolean spawns(Recipe recipe, @Nullable String[][] crops, int row, int col) {
		int size = recipe.size();
		Map<String, Integer> around = new HashMap<>();
		for (int r = Math.max(row - 1, 0); r <= Math.min(row + size, GRID_SIZE - 1); r++) {
			for (int c = Math.max(col - 1, 0); c <= Math.min(col + size, GRID_SIZE - 1); c++) {
				String crop = crops[r][c];
				if (crop == null) continue;

				boolean inArea = r >= row && r < row + size && c >= col && c < col + size;
				if (inArea) return false;
				around.merge(crop, 1, Integer::sum);
			}
		}

		// Lonelily needs no crops around it at all
		if (recipe.neighbours().isEmpty()) return around.isEmpty();
		return recipe.neighbours().entrySet().stream().allMatch(needed -> around.getOrDefault(needed.getKey(), 0) >= needed.getValue());
	}

	private static int digit(char c) {
		return DIGITS.indexOf(c);
	}

	/**
	 * @param id   the name SkyLayouts uses
	 * @param crop the {@link GreenhouseCrops} name, or null for ground blocks
	 */
	record Kind(String id, @Nullable String crop) {
	}

	/**
	 * @param size       the width and height of the mutation in cells
	 * @param neighbours the crops it needs around it by {@link GreenhouseCrops} name, or none to need no crops around it
	 */
	record Recipe(int size, Map<String, Integer> neighbours) {
	}

	/**
	 * @param crops the crop names, indexed {@code [row][col]} from the top left, null where there is no crop
	 * @param spots where mutations spawn, indexed the same way, or null to work them out from {@link #MUTATION_RECIPES}
	 */
	private record Board(@Nullable String[][] crops, boolean @Nullable [][] spots) {
		private boolean hasCrop() {
			for (@Nullable String[] row : crops) {
				for (@Nullable String crop : row) {
					if (crop != null) return true;
				}
			}
			return false;
		}
	}
}
