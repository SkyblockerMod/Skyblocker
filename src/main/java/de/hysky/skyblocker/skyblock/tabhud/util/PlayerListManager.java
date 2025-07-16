package de.hysky.skyblocker.skyblock.tabhud.util;

import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class may be used to get data from the player list. It doesn't get its
 * data every frame, instead, a scheduler is used to update the data this class
 * is holding periodically. The list is sorted like in the vanilla game.
 */
public class PlayerListManager {

	public static boolean shouldUpdateNextTick = false;

	public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Regex");
	private static final Pattern PLAYERS_COLUMN_PATTERN = Pattern.compile("\\s*(Players \\(\\d+\\)|Island|Coop \\(\\d+\\))\\s*");
	private static final Pattern INFO_COLUMN_PATTERN = Pattern.compile("\\s*Info\\s*");

	/**
	 * The player list in tab.
	 */
	private static List<PlayerListEntry> playerList = new ArrayList<>(); // Initialize to prevent npe.

	/**
	 * The player list in tab, but a list of strings instead of {@link PlayerListEntry}s.
	 *
	 * @implNote All leading and trailing whitespace are removed from the strings.
	 */
	private static List<String> playerStringList = new ArrayList<>();
	@Nullable
	private static String footer;
	private static final Map<String, TabListWidget> WIDGET_MAP = new Object2ObjectOpenHashMap<>();
	private static final Set<Runnable> LISTENERS = new ObjectOpenHashSet<>(); // this might not actually be a set due to how lambdas work
	private static final Set<String> HANDLED_TAB_WIDGETS = new ObjectOpenHashSet<>();

	public static @Nullable TabListWidget getListWidget(String name) {
		return WIDGET_MAP.get(name);
	}

	public static List<Text> createErrorMessage(String widgetName) {
		// TODO translatable
		// TODO actually add the command
		return List.of(
				Text.literal("Missing data for ").append(Text.literal(widgetName).formatted(Formatting.YELLOW)).append(" widget!"),
				Text.literal("Run ").append(Text.literal("/skyblocker tab").formatted(Formatting.GOLD)).append(" for more info.")
		);
	}

	public static void addHandledTabWidget(String name) {
		HANDLED_TAB_WIDGETS.add(name);
	}

	public static void registerListener(Runnable listener) {
		LISTENERS.add(listener);
	}

	public static void tryUpdateList() {
		if (shouldUpdateNextTick) {
			updateList();
			shouldUpdateNextTick = false;
		}
	}

	public static void updateList() {
		if (!Utils.isOnSkyblock()) return;
		ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();

		// check is needed, else game crashes on server leave
		if (networkHandler != null) {
			playerList = networkHandler.getPlayerList()
			                           .stream()
			                           .sorted(PlayerListHudAccessor.getOrdering())
			                           .toList();
			playerStringList = playerList.stream()
			                             .map(PlayerListEntry::getDisplayName)
			                             .filter(Objects::nonNull)
			                             .map(Text::getString)
			                             .map(String::strip)
			                             .toList();
		}

		final Predicate<String> playersColumnPredicate = PLAYERS_COLUMN_PATTERN.asMatchPredicate();
		final Predicate<String> infoColumnPredicate = INFO_COLUMN_PATTERN.asMatchPredicate();

		Text sideThing = Text.empty();
		List<Text> contents = new ArrayList<>();
		List<PlayerListEntry> playerListEntries = new ArrayList<>();

		boolean doingPlayers = false;
		boolean playersDone = false;
		IntObjectPair<String> hypixelWidgetName = IntObjectPair.of(0xFFFF00, "");

		WIDGET_MAP.clear();
		for (PlayerListEntry playerListEntry : playerList) {
			Text displayName = playerListEntry.getDisplayName();
			if (displayName == null) continue;
			String string = displayName.getString();

			if (string.isBlank()) continue;
			if (!playersDone) {
				// check if Players (number)
				if (playersColumnPredicate.test(string)) {
					if (!doingPlayers) {
						doingPlayers = true;
						// noinspection DataFlowIssue
						hypixelWidgetName = IntObjectPair.of(Formatting.AQUA.getColorValue(), "Players");
					}
					continue;
				}
				// Check if info, if it is, dip out
				if (infoColumnPredicate.test(string)) {
					playersDone = true;
					WIDGET_MAP.put(hypixelWidgetName.right(), new TabListWidget(Text.empty(), contents, playerListEntries));
					contents.clear();
					playerListEntries.clear();
					continue;
				}
			} else {
				if (infoColumnPredicate.test(string)) continue;
				// New widget alert!!!!
				// Now check for : because of the farming contest ACTIVE
				// Check for mining event minutes CUZ THEY FUCKING FORGOT THE SPACE iefzeoifzeoifomezhif
				if (!string.startsWith(" ") && string.contains(":") && (!hypixelWidgetName.right().startsWith("Mining Event") || !string.toLowerCase().startsWith("ends in"))) {
					if (!contents.isEmpty()) WIDGET_MAP.put(hypixelWidgetName.right(), new TabListWidget(sideThing, contents, playerListEntries));

					sideThing = Text.empty();
					contents.clear();
					playerListEntries.clear();

					Pair<IntObjectPair<String>, ? extends Text> nameAndInfo = getNameAndInfo(displayName);
					hypixelWidgetName = nameAndInfo.left();
					if (!HANDLED_TAB_WIDGETS.contains(hypixelWidgetName.right())) {
						WidgetManager.addWidgetInstance(new DefaultTabHudWidget(hypixelWidgetName.right(), Text.literal(hypixelWidgetName.right()).formatted(Formatting.BOLD), hypixelWidgetName.firstInt()));
					}
					if (!nameAndInfo.right().getString().isBlank()) {
						sideThing = trim(nameAndInfo.right());
						playerListEntries.add(playerListEntry);
					}
					continue;
				}
			}
			// Add the line to the content
			contents.add(trim(displayName));
			playerListEntries.add(playerListEntry);
		}

		if (!contents.isEmpty()) WIDGET_MAP.put(hypixelWidgetName.right(), new TabListWidget(sideThing, contents, playerListEntries));

		LISTENERS.forEach(Runnable::run);
	}

	private static Text trim(Text text) {
		List<Text> trimmedParts = new ArrayList<>();
		AtomicBoolean leadingSpaceFound = new AtomicBoolean(false);

		// leading spaces
		text.visit((style, asString) -> {
			String trimmed = asString;
			if (!leadingSpaceFound.get()) {
				trimmed = trimmed.stripLeading();
				if (!trimmed.isBlank()) leadingSpaceFound.set(true);
				else return Optional.empty();
			}
			trimmedParts.add(Text.literal(trimmed).setStyle(style));
			return Optional.empty();
		}, Style.EMPTY);

		// trailing spaces
		for (int i = 0; i < trimmedParts.size(); i++) {
			Text last = trimmedParts.removeLast();
			String trimmed = last.getString().stripTrailing();
			if (!trimmed.isBlank()) {
				trimmedParts.add(Text.literal(trimmed).setStyle(last.getStyle()));
				break;
			}
		}

		MutableText out = Text.empty();
		trimmedParts.forEach(out::append);

		return out;
	}

	/**
	 * @param text a line of text that contains a : from the tab
	 * @return a pair containing:
	 * <ul>
	 * <li> an int and string pair for the color and the widget name </li>
	 * <li> a text with the extra info sometimes shown on the right of the : </li>
	 * </ul>
	 */
	private static Pair<IntObjectPair<String>, ? extends Text> getNameAndInfo(Text text) {
		ObjectObjectMutablePair<String, MutableText> toReturn = new ObjectObjectMutablePair<>("", Text.empty());
		AtomicBoolean inInfo = new AtomicBoolean(false);
		AtomicInteger colorOutput = new AtomicInteger(0xFFFF00);
		text.visit((style, asString) -> {
			if (inInfo.get()) {
				toReturn.right().append(Text.literal(asString).fillStyle(style));
			} else {
				if (asString.contains(":")) {
					inInfo.set(true);
					String[] split = asString.split(":", 2);
					toReturn.left(toReturn.left() + split[0]);
					toReturn.right().append(Text.literal(split[1]).fillStyle(style));
					if (style.getColor() != null) colorOutput.set(style.getColor().getRgb());
				} else {
					toReturn.left(toReturn.left() + asString);
				}
			}
			return Optional.empty();
		}, Style.EMPTY);

		return Pair.of(IntObjectPair.of(colorOutput.get(), toReturn.left()), toReturn.right());
	}

	/**
	 * @return the cached player list
	 */
	public static List<PlayerListEntry> getPlayerList() {
		return playerList;
	}

	/**
	 * @return the cached player list as a list of strings
	 */
	public static List<String> getPlayerStringList() {
		return playerStringList;
	}

	public static void updateFooter(Text f) {
		if (f == null) {
			footer = null;
		} else {
			footer = f.getString();
			if (footer.isEmpty()) {
				footer = null;
			}
		}
	}

	@Nullable
	public static String getFooter() {
		return footer;
	}

	/**
	 * Get the display name at some index of the player list and apply a pattern to
	 * it
	 *
	 * @return the matcher if p fully matches, else null
	 */
	public static Matcher regexAt(int idx, Pattern p) {

		String str = PlayerListManager.strAt(idx);

		if (str == null) {
			return null;
		}

		Matcher m = p.matcher(str);
		if (!m.matches()) {
			LOGGER.error("no match: \"{}\" against \"{}\"", str, p);
			return null;
		} else {
			return m;
		}
	}

	/**
	 * Get the display name at some index of the player list as string
	 *
	 * @return the string or null, if the display name is null, empty or whitespace
	 * only
	 */
	public static String strAt(int idx) {

		if (playerList == null) {
			return null;
		}

		if (playerList.size() <= idx) {
			return null;
		}

		Text txt = playerList.get(idx).getDisplayName();
		if (txt == null) {
			return null;
		}
		String str = txt.getString().trim();
		if (str.isEmpty()) {
			return null;
		}
		return str;
	}

	/**
	 * Get the display name at some index of the player list as Text as seen in the
	 * game
	 *
	 * @return the PlayerListEntry at that index
	 */
	public static PlayerListEntry getRaw(int idx) {
		return playerList.get(idx);
	}

	public static int getSize() {
		return playerList.size();
	}

	private static final class DefaultTabHudWidget extends TabHudWidget {

		private DefaultTabHudWidget(String hypixelWidgetName, MutableText title, int color) {
			super(hypixelWidgetName, title, color, new Information(nameToId(hypixelWidgetName), title.copyContentOnly().append(Text.literal(" (auto)").formatted(Formatting.GRAY, Formatting.ITALIC))));
		}

		@Override
		protected void updateContent(List<Text> lines) {
			for (Text line : lines) {
				addComponent(new PlainTextComponent(line));
			}
		}
	}

	/**
	 * @param detail The text after the : on the widget's name. {@link Text#empty()} if there is none.
	 * @param lines The different lines, trimmed.
	 * @param playerListEntries The player list entries, unprocessed. If detail is present, the whole line is included as the first line in the list.
	 */
	public record TabListWidget(Text detail, List<Text> lines, List<PlayerListEntry> playerListEntries) {
		public TabListWidget(Text detail, List<Text> lines, List<PlayerListEntry> playerListEntries) {
			this.detail = detail.copy();
			this.lines = lines.stream().map(Text::copy).collect(Collectors.toList());
			this.playerListEntries = List.copyOf(playerListEntries);
		}
	}
}
