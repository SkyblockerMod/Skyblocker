package de.hysky.skyblocker.skyblock.tabhud.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor;
import de.hysky.skyblocker.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * This class may be used to get data from the player list. It doesn't get its
 * data every frame, instead, a scheduler is used to update the data this class
 * is holding periodically. The list is sorted like in the vanilla game.
 */
public class PlayerListMgr {

	public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Regex");

	private static List<PlayerListEntry> playerList;
    private static String footer;

	public static void updateList() {

		if (!Utils.isOnSkyblock()) {
			return;
		}

		ClientPlayNetworkHandler cpnwh = MinecraftClient.getInstance().getNetworkHandler();

		// check is needed, else game crashes on server leave
		if (cpnwh != null) {
			playerList = cpnwh.getPlayerList().stream().sorted(PlayerListHudAccessor.getOrdering()).toList();
		}
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

}
