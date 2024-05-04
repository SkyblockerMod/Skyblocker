package de.hysky.skyblocker.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.MessageHandlerAccessor;
import de.hysky.skyblocker.skyblock.item.MuseumItemCache;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility variables and methods for retrieving Skyblock related information.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String ALTERNATE_HYPIXEL_ADDRESS = System.getProperty("skyblocker.alternateHypixelAddress", "");

    private static final String PROFILE_PREFIX = "Profile: ";
    private static final String PROFILE_MESSAGE_PREFIX = "§aYou are playing on profile: §e";
    public static final String PROFILE_ID_PREFIX = "Profile ID: ";
    private static boolean isOnHypixel = false;
    private static boolean isOnSkyblock = false;
    private static boolean isInjected = false;
    /**
     * Current Skyblock location (from /locraw)
     */
    @NotNull
    private static Location location = Location.UNKNOWN;
    /**
     * The profile name parsed from the player list.
     */
    @NotNull
    private static String profile = "";
    /**
     * The profile id parsed from the chat.
     */
    @NotNull
    private static String profileId = "";
    /**
     * The following fields store data returned from /locraw: {@link #server}, {@link #gameType}, {@link #locationRaw}, and {@link #map}.
     */
    @SuppressWarnings("JavadocDeclaration")
    @NotNull
    private static String server = "";
    @NotNull
    private static String gameType = "";
    @NotNull
    private static String locationRaw = "";
    @NotNull
    private static String map = "";
    private static long clientWorldJoinTime = 0;
    private static boolean sentLocRaw = false;
    private static boolean canSendLocRaw = false;

    private static String mayor = "";

    /**
     * @implNote The parent text will always be empty, the actual text content is inside the text's siblings.
     */
    public static final ObjectArrayList<Text> TEXT_SCOREBOARD = new ObjectArrayList<>();
    public static final ObjectArrayList<String> STRING_SCOREBOARD = new ObjectArrayList<>();

    public static boolean isOnHypixel() {
        return isOnHypixel;
    }

    public static boolean isOnSkyblock() {
        return isOnSkyblock;
    }

    public static boolean isInDungeons() {
        return location == Location.DUNGEON || FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isInCrystalHollows() {
        return location == Location.CRYSTAL_HOLLOWS || FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isInDwarvenMines() {
        return location == Location.DWARVEN_MINES || location == Location.GLACITE_MINESHAFT || FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isInTheRift() {
        return location == Location.THE_RIFT;
    }

    /**
     * @return if the player is in the end island
     */
    public static boolean isInTheEnd() {
        return location == Location.THE_END;
    }

    public static boolean isInKuudra() {
        return location == Location.KUUDRAS_HOLLOW;
    }

    public static boolean isInjected() {
        return isInjected;
    }

    /**
     * @return the profile parsed from the player list.
     */
    @NotNull
    public static String getProfile() {
        return profile;
    }

    @NotNull
    public static String getProfileId() {
        return profileId;
    }

    /**
     * @return the location parsed from /locraw.
     */
    @NotNull
    public static Location getLocation() {
        return location;
    }

    /**
     * @return the server parsed from /locraw.
     */
    @NotNull
    public static String getServer() {
        return server;
    }

    /**
     * @return the game type parsed from /locraw.
     */
    @NotNull
    public static String getGameType() {
        return gameType;
    }

    /**
     * @return the location raw parsed from /locraw.
     */
    @NotNull
    public static String getLocationRaw() {
        return locationRaw;
    }

    /**
     * @return the map parsed from /locraw.
     */
    @NotNull
    public static String getMap() {
        return map;
    }

    /**
     * @return the current mayor as cached on skyblock join.
     */
    @NotNull
    public static String getMayor() {
        return mayor;
    }

    public static void init() {
        SkyblockEvents.JOIN.register(() -> tickMayorCache(false));
        ClientPlayConnectionEvents.JOIN.register(Utils::onClientWorldJoin);
        ClientReceiveMessageEvents.ALLOW_GAME.register(Utils::onChatMessage);
        ClientReceiveMessageEvents.GAME_CANCELED.register(Utils::onChatMessage); // Somehow this works even though onChatMessage returns a boolean
        Scheduler.INSTANCE.scheduleCyclic(() -> tickMayorCache(true), 24_000, true); // Update every 20 minutes
    }

    /**
     * Updates all the fields stored in this class from the sidebar, player list, and /locraw.
     */
    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        updateScoreboard(client);
        updatePlayerPresenceFromScoreboard(client);
        updateFromPlayerList(client);
        updateLocRaw();
    }

    /**
     * Updates {@link #isOnSkyblock}, {@link #isInDungeons}, and {@link #isInjected} from the scoreboard.
     */
    public static void updatePlayerPresenceFromScoreboard(MinecraftClient client) {
        List<String> sidebar = STRING_SCOREBOARD;

        FabricLoader fabricLoader = FabricLoader.getInstance();
        if (client.world == null || client.isInSingleplayer() || sidebar.isEmpty()) {
            if (fabricLoader.isDevelopmentEnvironment()) {
                sidebar = Collections.emptyList();
            } else {
                isOnSkyblock = false;
                return;
            }
        }

        if (sidebar.isEmpty() && !fabricLoader.isDevelopmentEnvironment()) return;

        if (fabricLoader.isDevelopmentEnvironment() || isConnectedToHypixel(client)) {
            if (!isOnHypixel) {
                isOnHypixel = true;
            }
            if (fabricLoader.isDevelopmentEnvironment() || sidebar.get(0).contains("SKYBLOCK") || sidebar.get(0).contains("SKIBLOCK")) {
                if (!isOnSkyblock) {
                    if (!isInjected) {
                        isInjected = true;
                        ItemTooltipCallback.EVENT.register(ItemTooltip::getTooltip);
                    }
                    isOnSkyblock = true;
                    SkyblockEvents.JOIN.invoker().onSkyblockJoin();
                }
            } else {
                onLeaveSkyblock();
            }
        } else if (isOnHypixel) {
            isOnHypixel = false;
            onLeaveSkyblock();
        }
    }

    private static boolean isConnectedToHypixel(MinecraftClient client) {
        String serverAddress = (client.getCurrentServerEntry() != null) ? client.getCurrentServerEntry().address.toLowerCase() : "";
        String serverBrand = (client.player != null && client.player.networkHandler != null && client.player.networkHandler.getBrand() != null) ? client.player.networkHandler.getBrand() : "";

        return serverAddress.equalsIgnoreCase(ALTERNATE_HYPIXEL_ADDRESS) || serverAddress.contains("hypixel.net") || serverAddress.contains("hypixel.io") || serverBrand.contains("Hypixel BungeeCord");
    }

    private static void onLeaveSkyblock() {
        if (isOnSkyblock) {
            isOnSkyblock = false;
            SkyblockEvents.LEAVE.invoker().onSkyblockLeave();
        }
    }

    public static String getIslandArea() {
        try {
            for (String sidebarLine : STRING_SCOREBOARD) {
                if (sidebarLine.contains("⏣") || sidebarLine.contains("ф") /* Rift */) {
                    return sidebarLine.strip();
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("[Skyblocker] Failed to get location from sidebar", e);
        }
        return "Unknown";
    }

    public static double getPurse() {
        String purseString = null;
        double purse = 0;

        try {
            for (String sidebarLine : STRING_SCOREBOARD) {
                if (sidebarLine.contains("Piggy:") || sidebarLine.contains("Purse:")) purseString = sidebarLine;
            }
            if (purseString != null) purse = Double.parseDouble(purseString.replaceAll("[^0-9.]", "").strip());
            else purse = 0;

        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("[Skyblocker] Failed to get purse from sidebar", e);
        }
        return purse;
    }

    public static int getBits() {
        int bits = 0;
        String bitsString = null;
        try {
            for (String sidebarLine : STRING_SCOREBOARD) {
                if (sidebarLine.contains("Bits")) bitsString = sidebarLine;
            }
            if (bitsString != null) {
                bits = Integer.parseInt(bitsString.replaceAll("[^0-9.]", "").strip());
            }
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("[Skyblocker] Failed to get bits from sidebar", e);
        }
        return bits;
    }

    private static void updateScoreboard(MinecraftClient client) {
        try {
            TEXT_SCOREBOARD.clear();
            STRING_SCOREBOARD.clear();

            ClientPlayerEntity player = client.player;
            if (player == null) return;

            Scoreboard scoreboard = player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            ObjectArrayList<Text> textLines = new ObjectArrayList<>();
            ObjectArrayList<String> stringLines = new ObjectArrayList<>();

            for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
                //Limit to just objectives displayed in the scoreboard (specifically sidebar objective)
                if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());

                    if (team != null) {
                        Text textLine = Text.empty().append(team.getPrefix().copy()).append(team.getSuffix().copy());
                        String strLine = team.getPrefix().getString() + team.getSuffix().getString();

                        if (!strLine.trim().isEmpty()) {
                            String formatted = Formatting.strip(strLine);

                            textLines.add(textLine);
                            stringLines.add(formatted);
                        }
                    }
                }
            }

            if (objective != null) {
                stringLines.add(objective.getDisplayName().getString());
                textLines.add(Text.empty().append(objective.getDisplayName().copy()));

                Collections.reverse(stringLines);
                Collections.reverse(textLines);
            }

            TEXT_SCOREBOARD.addAll(textLines);
            STRING_SCOREBOARD.addAll(stringLines);
        } catch (NullPointerException e) {
            //Do nothing
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
            if (!sentLocRaw && canSendLocRaw && currentTime > clientWorldJoinTime + 1000) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/locraw");
                sentLocRaw = true;
                canSendLocRaw = false;
            }
        } else {
            resetLocRawInfo();
        }
    }

    /**
     * Parses /locraw chat message and updates {@link #server}, {@link #gameType}, {@link #locationRaw}, {@link #map}
     * and {@link #location}
     *
     * @param message json message from chat
     */
    private static void parseLocRaw(String message) {
        JsonObject locRaw = JsonParser.parseString(message).getAsJsonObject();

        if (locRaw.has("server")) {
            server = locRaw.get("server").getAsString();
        }
        if (locRaw.has("gameType")) {
            gameType = locRaw.get("gameType").getAsString();
        }
        if (locRaw.has("mode")) {
            locationRaw = locRaw.get("mode").getAsString();
            location = Location.from(locationRaw);
        } else {
            location = Location.UNKNOWN;
        }
        if (locRaw.has("map")) {
            map = locRaw.get("map").getAsString();
        }
    }

    /**
     * Parses the /locraw reply from the server and updates the player's profile id
     *
     * @return not display the message in chat if the command is sent by the mod
     */
    public static boolean onChatMessage(Text text, boolean overlay) {
        String message = text.getString();

        if (message.startsWith("{\"server\":") && message.endsWith("}")) {
            parseLocRaw(message);
            boolean shouldFilter = !sentLocRaw;
            sentLocRaw = false;

            return shouldFilter;
        }

        if (isOnSkyblock) {
            if (message.startsWith(PROFILE_MESSAGE_PREFIX)) {
                profile = message.substring(PROFILE_MESSAGE_PREFIX.length()).split("§b")[0];
            } else if (message.startsWith(PROFILE_ID_PREFIX)) {
                profileId = message.substring(PROFILE_ID_PREFIX.length());

                MuseumItemCache.tick(profileId);
            }
        }

        return true;
    }

    private static void resetLocRawInfo() {
        sentLocRaw = false;
        canSendLocRaw = true;
        server = "";
        gameType = "";
        locationRaw = "";
        map = "";
        location = Location.UNKNOWN;
    }

    private static void tickMayorCache(boolean refresh) {
        if (!mayor.isEmpty() && !refresh) return;

        CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject json = JsonParser.parseString(Http.sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/election")).getAsJsonObject();
                if (json.get("success").getAsBoolean()) return json.get("mayor").getAsJsonObject().get("name").getAsString();
                throw new IOException(json.get("cause").getAsString());
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to get mayor status!", e);
            }
            return "";
        }).thenAccept(s -> {
            if (!s.isEmpty()) mayor = s;
        });

    }

    /**
     * Used to avoid triggering things like chat rules or chat listeners infinitely, do not use otherwise.
     * 
     * Bypasses MessageHandler#onGameMessage
     */
    public static void sendMessageToBypassEvents(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();

        client.inGameHud.getChatHud().addMessage(message);
        ((MessageHandlerAccessor) client.getMessageHandler()).invokeAddToChatLog(message, Instant.now());
        client.getNarratorManager().narrateSystemMessage(message);
    }
}
