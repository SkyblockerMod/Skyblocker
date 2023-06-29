package me.xmrvizzy.skyblocker.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.skyblock.rift.TheRift;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility variables and methods for retrieving Skyblock related information.
 */
public class Utils {
    private static final String PROFILE_PREFIX = "Profile: ";
    private static boolean isOnSkyblock = false;
    private static boolean isInDungeons = false;
    private static boolean isInjected = false;
    /**
     * The following fields store data returned from /locraw: {@link #profile}, {@link #server}, {@link #gameType}, {@link #locationRaw}, and {@link #map}.
     */
    @SuppressWarnings("JavadocDeclaration")
    private static String profile = "";
    private static String server = "";
    private static String gameType = "";
    private static String locationRaw = "";
    private static String map = "";
    private static long clientWorldJoinTime = 0;
    private static boolean sentLocRaw = false;
    private static long lastLocRaw = 0;

    public static boolean isOnSkyblock() {
        return isOnSkyblock;
    }

    public static boolean isInDungeons() {
        return isInDungeons;
    }

    public static boolean isInTheRift() {
        return getLocationRaw().equals(TheRift.LOCATION);
    }

    public static boolean isInjected() {
        return isInjected;
    }

    /**
     * @return the profile parsed from the player list.
     */
    public static String getProfile() {
        return profile;
    }

    /**
     * @return the server parsed from /locraw.
     */
    public static String getServer() {
        return server;
    }

    /**
     * @return the game type parsed from /locraw.
     */
    public static String getGameType() {
        return gameType;
    }

    /**
     * @return the location raw parsed from /locraw.
     */
    public static String getLocationRaw() {
        return locationRaw;
    }

    /**
     * @return the map parsed from /locraw.
     */
    public static String getMap() {
        return map;
    }

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(Utils::onClientWorldJoin);
        ClientReceiveMessageEvents.ALLOW_GAME.register(Utils::onChatMessage);
        ClientReceiveMessageEvents.GAME_CANCELED.register(Utils::onChatMessage); // Somehow this works even though onChatMessage returns a boolean
    }

    /**
     * Updates all the fields stored in this class from the sidebar, player list, and /locraw.
     */
    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        updateFromScoreboard(client);
        updateFromPlayerList(client);
        updateLocRaw();
    }

    /**
     * Updates {@link #isOnSkyblock}, {@link #isInDungeons}, and {@link #isInjected} from the scoreboard.
     */
    public static void updateFromScoreboard(MinecraftClient client) {
        List<String> sidebar;

        if (client.world == null || client.isInSingleplayer() || (sidebar = getSidebar()) == null) {
            isOnSkyblock = false;
            isInDungeons = false;
            return;
        }
        String string = sidebar.toString();

        if (sidebar.isEmpty()) return;
        if (sidebar.get(0).contains("SKYBLOCK") || sidebar.get(0).contains("SKIBLOCK")) {
            if (!isOnSkyblock) {
                if (!isInjected) {
                    isInjected = true;
                    ItemTooltipCallback.EVENT.register(PriceInfoTooltip::onInjectTooltip);
                }
                isOnSkyblock = true;
                SkyblockEvents.JOIN.invoker().onSkyblockJoin();
            }
        } else if (isOnSkyblock) {
            isOnSkyblock = false;
            isInDungeons = false;
            SkyblockEvents.LEAVE.invoker().onSkyblockLeave();
        }
        isInDungeons = isOnSkyblock && string.contains("The Catacombs");
    }

    public static String getLocation() {
        String location = null;
        List<String> sidebarLines = getSidebar();
        try {
            if (sidebarLines != null) {
                for (String sidebarLine : sidebarLines) {
                    if (sidebarLine.contains("⏣")) location = sidebarLine;
                    if (sidebarLine.contains("ф")) location = sidebarLine; //Rift
                }
                if (location == null) location = "Unknown";
                location = location.strip();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return location;
    }

    public static double getPurse() {
        String purseString = null;
        double purse = 0;

        List<String> sidebarLines = getSidebar();
        try {

            if (sidebarLines != null) {
                for (String sidebarLine : sidebarLines) {
                    if (sidebarLine.contains("Piggy:")) purseString = sidebarLine;
                    if (sidebarLine.contains("Purse:")) purseString = sidebarLine;
                }
            }
            if (purseString != null) purse = Double.parseDouble(purseString.replaceAll("[^0-9.]", "").strip());
            else purse = 0;

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return purse;
    }

    public static int getBits() {
        int bits = 0;
        String bitsString = null;
        List<String> sidebarLines = getSidebar();
        try {
            if (sidebarLines != null) {
                for (String sidebarLine : sidebarLines) {
                    if (sidebarLine.contains("Bits")) bitsString = sidebarLine;
                }
            }
            if (bitsString != null) {
                bits = Integer.parseInt(bitsString.replaceAll("[^0-9.]", "").strip());
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return bits;
    }


    public static List<String> getSidebar() {
        try {
            ClientPlayerEntity client = MinecraftClient.getInstance().player;
            if (client == null) return Collections.emptyList();
            Scoreboard scoreboard = MinecraftClient.getInstance().player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
            List<String> lines = new ArrayList<>();
            for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(objective)) {
                Team team = scoreboard.getPlayerTeam(score.getPlayerName());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if (line.trim().length() > 0) {
                        String formatted = Formatting.strip(line);
                        lines.add(formatted);
                    }
                }
            }

            if (objective != null) {
                lines.add(objective.getDisplayName().getString());
                Collections.reverse(lines);
            }
            return lines;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static void updateFromPlayerList(MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            return;
        }
        for (PlayerListEntry playerListEntry : client.getNetworkHandler().getPlayerList()) {
            if (playerListEntry.getDisplayName() == null) {
                continue;
            }
            String name = playerListEntry.getDisplayName().getString();
            if (name.startsWith(PROFILE_PREFIX)) {
                profile = name.substring(PROFILE_PREFIX.length());
            }
        }
    }

    public static void onClientWorldJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        clientWorldJoinTime = System.currentTimeMillis();
        resetLocRawInfo();
    }

    /**
     * Sends /locraw to the server if the player is on skyblock and on a new island.
     */
    private static void updateLocRaw() {
        if (isOnSkyblock) {
            long currentTime = System.currentTimeMillis();
            if (!sentLocRaw && currentTime > clientWorldJoinTime + 1000 && currentTime > lastLocRaw + 15000) {
                SkyblockerMod.getInstance().messageScheduler.sendMessageAfterCooldown("/locraw");
                sentLocRaw = true;
                lastLocRaw = currentTime;
            }
        } else {
            resetLocRawInfo();
        }
    }

    /**
     * Parses the /locraw reply from the server
     *
     * @return not display the message in chat is the command is sent by the mod
     */
    public static boolean onChatMessage(Text text, boolean overlay) {
        String message = text.getString();
        if (message.startsWith("{\"server\":") && message.endsWith("}")) {
            JsonObject locRaw = JsonParser.parseString(message).getAsJsonObject();
            if (locRaw.has("server")) {
                server = locRaw.get("server").getAsString();
                if (locRaw.has("gameType")) {
                    gameType = locRaw.get("gameType").getAsString();
                }
                if (locRaw.has("mode")) {
                    locationRaw = locRaw.get("mode").getAsString();
                }
                if (locRaw.has("map")) {
                    map = locRaw.get("map").getAsString();
                }
                return !sentLocRaw;
            }
        }
        return true;
    }

    private static void resetLocRawInfo() {
        sentLocRaw = false;
        server = "";
        gameType = "";
        locationRaw = "";
        map = "";
    }
}