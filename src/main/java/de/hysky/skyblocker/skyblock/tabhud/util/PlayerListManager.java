package de.hysky.skyblocker.skyblock.tabhud.util;

import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
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

/**
 * This class may be used to get data from the player list. It doesn't get its
 * data every frame, instead, a scheduler is used to update the data this class
 * is holding periodically. The list is sorted like in the vanilla game.
 */
public class PlayerListManager {

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
	 * @implNote All leading and trailing whitespace is removed from the strings.
	 */
	private static List<String> playerStringList = new ArrayList<>();
	@Nullable
	private static String footer;
	public static final Map<String, TabHudWidget> tabWidgetInstances = new Object2ObjectOpenHashMap<>();
	public static final List<TabHudWidget> tabWidgetsToShow = new ObjectArrayList<>(5);

	private static void reset() {
		if (!tabWidgetsToShow.isEmpty()) {
			tabWidgetsToShow.clear();
		}
	}

	// TODO: check for changes instead of updating every second
	public static void updateList() {
		if (!Utils.isOnSkyblock()) {
			reset();
			return;
		}

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

		if (!SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled) {
			reset();
			return;
		}

		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen widgetsConfigurationScreen && widgetsConfigurationScreen.isPreviewVisible()) return;

		if (Utils.isInDungeons()) updateDungeons(null);
		else updateWidgetsFrom(playerList);
	}

	/**
	 * Update specifically for dungeons cuz they don't use the new system I HATE THEM
	 *
	 * @param lines used for the config screen
	 */
	public static void updateDungeons(List<Text> lines) {
		if (lines != null) {
			// This is so wack I hate this
			playerList = new ArrayList<>();
			for (int i = 0; i < lines.size(); i++) {
				playerList.add(new PlayerListEntry(new GameProfile(UUID.randomUUID(), String.valueOf(i)), false));
				playerList.getLast().setDisplayName(lines.get(i));
			}
		}

		tabWidgetsToShow.clear();
		tabWidgetsToShow.add(getTabHudWidget("Dungeon Buffs", List.of()));
		tabWidgetsToShow.add(getTabHudWidget("Dungeon Deaths", List.of()));
		tabWidgetsToShow.add(getTabHudWidget("Dungeon Downed", List.of()));
		tabWidgetsToShow.add(getTabHudWidget("Dungeon Puzzles", List.of()));
		tabWidgetsToShow.add(getTabHudWidget("Dungeon Discoveries", List.of()));
		tabWidgetsToShow.add(getTabHudWidget("Dungeon Info", List.of()));
		for (int i = 1; i < 6; i++) {
			tabWidgetsToShow.add(getTabHudWidget("Dungeon Player " + i, List.of()));
		}

	}

	/**
	 * Update the tab widgets using a list of text representing the lines of the in-game TAB
	 *
	 * @param lines in-game TAB
	 */
	public static void updateWidgetsFrom(List<PlayerListEntry> lines) {
		final Predicate<String> playersColumnPredicate = PLAYERS_COLUMN_PATTERN.asMatchPredicate();
		final Predicate<String> infoColumnPredicate = INFO_COLUMN_PATTERN.asMatchPredicate();

		tabWidgetsToShow.clear();
		boolean doingPlayers = false;
		boolean playersDone = false;
		IntObjectPair<String> hypixelWidgetName = IntObjectPair.of(0xFFFF00, "");
		// These two lists should match each other.
		// playerListEntries is only used for the player list widget
		List<Text> contents = new ArrayList<>();
		List<PlayerListEntry> playerListEntries = new ArrayList<>();

		for (PlayerListEntry playerListEntry : lines) {
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
					if (!contents.isEmpty()) tabWidgetsToShow.add(getTabHudWidget(hypixelWidgetName, contents, playerListEntries));
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
					if (!contents.isEmpty()) tabWidgetsToShow.add(getTabHudWidget(hypixelWidgetName, contents, playerListEntries));
					contents.clear();
					playerListEntries.clear();
					Pair<IntObjectPair<String>, ? extends Text> nameAndInfo = getNameAndInfo(displayName);
					hypixelWidgetName = nameAndInfo.left();
					if (!nameAndInfo.right().getString().isBlank()) {
						contents.add(trim(nameAndInfo.right()));
						playerListEntries.add(playerListEntry);
					}
					continue;
				}
			}
			// Add the line to the content
			contents.add(trim(displayName));
			playerListEntries.add(playerListEntry);
		}
		if (!contents.isEmpty()) tabWidgetsToShow.add(getTabHudWidget(hypixelWidgetName, contents, playerListEntries));
		if (!tabWidgetsToShow.contains(tabWidgetInstances.get("Active Effects")) && SkyblockerConfigManager.get().uiAndVisuals.tabHud.effectsFromFooter) {
			tabWidgetsToShow.add(getTabHudWidget("Active Effects", List.of()));
		}
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

	private static TabHudWidget getTabHudWidget(IntObjectPair<String> hypixelWidgetName, List<Text> lines, @Nullable List<PlayerListEntry> playerListEntries) {
		TabHudWidget tabHudWidget;
		if (tabWidgetInstances.containsKey(hypixelWidgetName.right())) {
			tabHudWidget = tabWidgetInstances.get(hypixelWidgetName.right());
		} else {
			tabHudWidget = new DefaultTabHudWidget(hypixelWidgetName.right(), Text.literal(hypixelWidgetName.right()).formatted(Formatting.BOLD), hypixelWidgetName.firstInt());
			WidgetManager.addWidgetInstance(tabHudWidget);
		}
		tabHudWidget.updateFromTab(lines, playerListEntries);
		tabHudWidget.update();
		return tabHudWidget;
	}

	private static TabHudWidget getTabHudWidget(String hypixelWidgetName, List<Text> lines) {
		return getTabHudWidget(IntObjectPair.of(0xFFFF0000, hypixelWidgetName), lines, null);
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
	 * Gets the display name at some index of the player list
	 *
	 * @return the text or null, if the display name is null
	 * @implNote currently designed specifically for crimson isles faction quests
	 * widget and the rift widgets, might not work correctly without
	 * modification for other stuff. you've been warned!
	 */
	public static Text textAt(int idx) {

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

		// Rebuild the text object to remove leading space thats in all faction quest
		// stuff (also removes trailing space just in case)
		MutableText newText = Text.empty();
		int size = txt.getSiblings().size();

		for (int i = 0; i < size; i++) {
			Text current = txt.getSiblings().get(i);
			String textToAppend = current.getString();

			// Trim leading & trailing space - this can only be done at the start and end
			// otherwise it'll produce malformed results
			if (i == 0)
				textToAppend = textToAppend.stripLeading();
			if (i == size - 1)
				textToAppend = textToAppend.stripTrailing();

			newText.append(Text.literal(textToAppend).setStyle(current.getStyle()));
		}

		// Avoid returning an empty component - Rift advertisements needed this
		if (newText.getString().isEmpty()) {
			return null;
		}

		return newText;
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
			super(hypixelWidgetName, title, color);
		}

		@Override
		protected void updateContent(List<Text> lines) {
			lines.forEach(text -> addComponent(new PlainTextComponent(text)));
		}
	}

}
