package me.xmrvizzy.skyblocker.skyblock.tabhud.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.xmrvizzy.skyblocker.mixin.PlayerListHudAccessor;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class PlayerListMgr {

    public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Regex");

    private static List<PlayerListEntry> playerList;

    public static void updateList() {

        if (!Utils.isOnSkyblock) {
            return;
        }

        ClientPlayNetworkHandler cpnwh = MinecraftClient.getInstance().getNetworkHandler();

        // check is needed, else crash on server leave
        if (cpnwh != null) {

            playerList = cpnwh.getPlayerList()
                    .stream()
                    .sorted(PlayerListHudAccessor.getOrdering())
                    .toList();
        }
    }

    // apply pattern to entry at index of player list.
    // return null if there's nothing to match against in the entry,
    // or if the pattern doesn't fully match.
    public static Matcher regexAt(int idx, Pattern p) {

        String str = PlayerListMgr.strAt(idx);

        if (str == null) {
            return null;
        }

        Matcher m = p.matcher(str);
        if (!m.matches()) {
             LOGGER.debug("no match: \"{}\" against \"{}\"", str, p);
            return null;
        } else {
            return m;
        }
    }

    // return string (i.e. displayName) at index of player list.
    // return null if string is null, empty or whitespace only.
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
        if (str.length() == 0) {
            return null;
        }
        return str;
    }

    public static PlayerListEntry getRaw(int i) {
        return playerList.get(i);
    }

    public static int getSize() {
        return playerList.size();
    }

}
