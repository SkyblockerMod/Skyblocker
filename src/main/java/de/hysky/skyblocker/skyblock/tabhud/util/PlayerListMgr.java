package de.hysky.skyblocker.skyblock.tabhud.util;


import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class may be used to get data from the player list. It doesn't get its
 * data every frame, instead, a scheduler is used to update the data this class
 * is holding periodically. The list is sorted like in the vanilla game.
 */
public class PlayerListMgr {

	public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Regex");
	private static final Pattern PLAYERS_COLUMN_PATTERN = Pattern.compile("(^|\\s*)(Players \\(\\d+\\)|Island)(\\s*|$)");
	private static final Pattern INFO_COLUMN_PATTERN = Pattern.compile("(^|\\s*)Info(\\s*|$)");

	/**
	 * The player list in tab.
	 */
	private static List<PlayerListEntry> playerList = new ArrayList<>(); // Initialize to prevent npe.
	/**
	 * The player list in tab, but a list of strings instead of {@link PlayerListEntry}s.
	 */
	private static List<String> playerStringList = new ArrayList<>();
	private static String footer;
	public static final Map<String, TabHudWidget> tabWidgetInstances = new Object2ObjectOpenHashMap<>();
	public static final List<TabHudWidget> tabWidgetsToShow = new ObjectArrayList<>(5);

	public static void updateList() {

		if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled) {
			return;
		}

		ClientPlayNetworkHandler cpnwh = MinecraftClient.getInstance().getNetworkHandler();

		// check is needed, else game crashes on server leave
		if (cpnwh != null) {
			playerList = cpnwh.getPlayerList().stream().sorted(PlayerListHudAccessor.getOrdering()).toList();
			playerStringList = playerList.stream().map(PlayerListEntry::getDisplayName).filter(Objects::nonNull).map(Text::getString).map(String::strip).toList();
		}

		if (Utils.isInDungeons()) {
			updateDungeons(null);
		}
		else if (!(MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen widgetsConfigurationScreen && widgetsConfigurationScreen.isPreviewVisible())) {
			updateWidgetsFrom(playerList.stream().map(PlayerListEntry::getDisplayName).filter(Objects::nonNull).toList());
		}


	}

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

	public static void updateWidgetsFrom(List<Text> lines) {
		final Predicate<String> playersColumnPredicate = PLAYERS_COLUMN_PATTERN.asMatchPredicate();
		final Predicate<String> infoColumnPredicate = INFO_COLUMN_PATTERN.asMatchPredicate();

		tabWidgetsToShow.clear();
		boolean doingPlayers = false;
		boolean playersDone = false;
		String hypixelWidgetName = "";
		List<Text> contents = new ArrayList<>();

		for (Text displayName : lines) {
			String string = displayName.getString();

			if (string.isBlank()) continue;
			if (!playersDone) {
				// check if Players (number)
				if (playersColumnPredicate.test(string)) {
					if (!doingPlayers) {
						doingPlayers = true;
						hypixelWidgetName = "Players";
					}
					continue;
				}
				// Check if info, if it is, dip out
				if (infoColumnPredicate.test(string)) {
					playersDone = true;
					if (!contents.isEmpty()) tabWidgetsToShow.add(getTabHudWidget(hypixelWidgetName, contents));
					contents.clear();
					continue;
				}
			} else {
				if (infoColumnPredicate.test(string)) continue;
				// New widget alert!!!!
				// Now check for : because of the farming contest ACTIVE
				if (!string.startsWith(" ") && string.contains(":")) {
					if (!contents.isEmpty()) tabWidgetsToShow.add(getTabHudWidget(hypixelWidgetName, contents));
					contents.clear();
					Pair<String, ? extends Text> nameAndInfo = getNameAndInfo(displayName);
					hypixelWidgetName = nameAndInfo.left();
					if (!nameAndInfo.right().getString().isBlank()) contents.add(trim(nameAndInfo.right()));
					continue;
				}
			}
			// Add the line to the content
			contents.add(trim(displayName));
		}
		if (!contents.isEmpty()) tabWidgetsToShow.add(getTabHudWidget(hypixelWidgetName, contents));
		if (!tabWidgetsToShow.contains(tabWidgetInstances.get("Active Effects")) && SkyblockerConfigManager.get().uiAndVisuals.tabHud.effectsFromFooter) {
			tabWidgetsToShow.add(getTabHudWidget("Active Effects", List.of()));
		}
		ScreenBuilder.positionsNeedsUpdating = true;
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

	private static TabHudWidget getTabHudWidget(String hypixelWidgetName, List<Text> lines) {
		if (tabWidgetInstances.containsKey(hypixelWidgetName)) {
			TabHudWidget tabHudWidget = tabWidgetInstances.get(hypixelWidgetName);
			tabHudWidget.updateFromTab(lines);
			tabHudWidget.update();
			return tabHudWidget;
		} else {
			DefaultTabHudWidget defaultTabHudWidget = new DefaultTabHudWidget(hypixelWidgetName, Text.literal(hypixelWidgetName).formatted(Formatting.BOLD));
			tabWidgetInstances.put(defaultTabHudWidget.getHypixelWidgetName(), defaultTabHudWidget);
			defaultTabHudWidget.updateFromTab(lines);
			defaultTabHudWidget.update();
			return defaultTabHudWidget;
		}
	}

	private static Pair<String, ? extends Text> getNameAndInfo(Text text) {
		ObjectObjectMutablePair<String, MutableText> toReturn = new ObjectObjectMutablePair<>("", Text.empty());
		AtomicBoolean inInfo = new AtomicBoolean(false);
		text.visit((style, asString) -> {
			if (inInfo.get()) {
				toReturn.right().append(Text.literal(asString).fillStyle(style));
			} else {
				if (asString.contains(":")) {
					inInfo.set(true);
					String[] split = asString.split(":", 2);
					toReturn.left(toReturn.left() + split[0]);
					toReturn.right().append(Text.literal(split[1]).fillStyle(style));
				} else {
					toReturn.left(toReturn.left() + asString);
				}
			}
			return Optional.empty();
		}, Style.EMPTY);

		return toReturn;
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
		}
	}

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

		String str = PlayerListMgr.strAt(idx);

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
	 *         only
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
	 *
	 * @implNote currently designed specifically for crimson isles faction quests
	 *           widget and the rift widgets, might not work correctly without
	 *           modification for other stuff. you've been warned!
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
		public DefaultTabHudWidget(String hypixelWidgetName, MutableText title) {
			super(hypixelWidgetName, title, 0xFFFF00);
		}

		@Override
		protected void updateContent(List<Text> lines) {
			lines.forEach(text -> addComponent(new PlainTextComponent(text)));
		}
	}

}
